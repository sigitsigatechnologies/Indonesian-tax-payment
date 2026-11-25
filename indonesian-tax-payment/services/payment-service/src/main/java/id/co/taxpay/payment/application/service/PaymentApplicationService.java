package id.co.taxpay.payment.application.service;

import id.co.taxpay.common.error.ErrorCode;
import id.co.taxpay.common.web.CorrelationIdHolder;
import id.co.taxpay.payment.application.command.InitiatePaymentCommand;
import id.co.taxpay.payment.application.model.PageView;
import id.co.taxpay.payment.application.model.PaymentHistoryView;
import id.co.taxpay.payment.application.model.PaymentView;
import id.co.taxpay.payment.domain.exception.IdempotencyConflictException;
import id.co.taxpay.payment.domain.exception.PaymentException;
import id.co.taxpay.payment.domain.exception.PaymentNotFoundException;
import id.co.taxpay.payment.domain.model.PaymentApproval;
import id.co.taxpay.payment.domain.model.PaymentIdempotency;
import id.co.taxpay.payment.domain.model.PaymentStatus;
import id.co.taxpay.payment.domain.model.PaymentStatusHistory;
import id.co.taxpay.payment.domain.model.TaxPayment;
import id.co.taxpay.payment.domain.port.AuditLogRepository;
import id.co.taxpay.payment.domain.port.PaymentApprovalRepository;
import id.co.taxpay.payment.domain.port.PaymentIdempotencyRepository;
import id.co.taxpay.payment.domain.port.PaymentStatusHistoryRepository;
import id.co.taxpay.payment.domain.port.TaxPaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentApplicationService {

    private static final Duration IDEMPOTENCY_TTL = Duration.ofHours(24);
    private static final String CHECKER_ROLE = "CHECKER";
    private static final DateTimeFormatter REFERENCE_DATE = DateTimeFormatter.ofPattern("yyyyMMdd")
            .withZone(ZoneOffset.UTC);

    private final TaxPaymentRepository taxPayments;
    private final PaymentStatusHistoryRepository statusHistories;
    private final PaymentIdempotencyRepository idempotencyRepository;
    private final PaymentApprovalRepository approvals;
    private final AuditLogRepository auditLogs;
    private final PaymentEventPublisher eventPublisher;
    private final PaymentSagaOrchestrator sagaOrchestrator;
    private final TransactionTemplate transactionTemplate;
    private final Clock clock;

    public PaymentView initiate(InitiatePaymentCommand command, String idempotencyKey, String actor) {
        String requestHash = hash(command);
        String normalizedActor = normalizeActor(actor);
        Instant now = clock.instant();

        var existing = idempotencyRepository.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()) {
            PaymentIdempotency record = existing.get();
            if (record.expiresAt().isBefore(now)) {
                idempotencyRepository.deleteByIdempotencyKey(idempotencyKey);
            } else {
                return resolveExisting(record, requestHash);
            }
        }

        try {
            PaymentView view = Objects.requireNonNull(
                    transactionTemplate.execute(status ->
                            createPayment(command, idempotencyKey, requestHash, normalizedActor, now)),
                    "Payment creation returned no result");
            sagaOrchestrator.startAfterInitiation(view.paymentId());
            return view;
        } catch (DataIntegrityViolationException ex) {
            return idempotencyRepository.findByIdempotencyKey(idempotencyKey)
                    .map(record -> resolveExisting(record, requestHash))
                    .orElseThrow(() -> ex);
        }
    }

    @Transactional(readOnly = true)
    public PaymentView getById(UUID paymentId) {
        return taxPayments.findById(paymentId)
                .map(this::toView)
                .orElseThrow(() -> new PaymentNotFoundException("Payment " + paymentId + " was not found"));
    }

    @Transactional(readOnly = true)
    public PaymentView getByReference(String reference) {
        return taxPayments.findByReference(reference)
                .map(this::toView)
                .orElseThrow(() -> new PaymentNotFoundException("Payment reference " + reference + " was not found"));
    }

    @Transactional(readOnly = true)
    public List<PaymentHistoryView> getHistory(UUID paymentId) {
        if (taxPayments.findById(paymentId).isEmpty()) {
            throw new PaymentNotFoundException("Payment " + paymentId + " was not found");
        }
        return statusHistories.findByPaymentId(paymentId).stream()
                .map(this::toHistoryView)
                .toList();
    }

    @Transactional(readOnly = true)
    public PageView<PaymentView> listByCustomer(String customerId, int page, int size) {
        int offset = page * size;
        List<PaymentView> content = taxPayments.findByCustomerId(customerId, offset, size).stream()
                .map(this::toView)
                .toList();
        long total = taxPayments.countByCustomerId(customerId);
        return PageView.of(content, page, size, total);
    }

    public PaymentView reverse(UUID paymentId, String reason, String actor) {
        return sagaOrchestrator.reverse(paymentId, reason, normalizeActor(actor));
    }

    @Transactional
    public PaymentView transition(UUID paymentId, PaymentStatus targetStatus, String reason, String actor) {
        TaxPayment payment = loadPayment(paymentId);
        return toView(applyTransition(payment, targetStatus, reason, normalizeActor(actor)));
    }

    @Transactional
    public PaymentView approve(UUID paymentId, String checkerId, String role, String notes) {
        requireCheckerRole(role);
        String checker = normalizeActor(checkerId);
        TaxPayment payment = loadPayment(paymentId);
        requireAwaitingApproval(payment);
        PaymentApproval approval = approvals.findByPaymentId(paymentId)
                .orElseThrow(() -> new PaymentException(ErrorCode.INVALID_STATE_TRANSITION,
                        "No approval record exists for payment " + paymentId));
        PaymentApproval decided = approval.approve(checker, notes, clock.instant());
        approvals.save(decided);
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                sagaOrchestrator.continueAfterApproval(paymentId);
            }
        });
        return toView(applyTransition(payment, PaymentStatus.APPROVED, "MAKER_CHECKER_APPROVED", checker));
    }

    @Transactional
    public PaymentView reject(UUID paymentId, String checkerId, String role, String notes) {
        requireCheckerRole(role);
        String checker = normalizeActor(checkerId);
        TaxPayment payment = loadPayment(paymentId);
        requireAwaitingApproval(payment);
        PaymentApproval approval = approvals.findByPaymentId(paymentId)
                .orElseThrow(() -> new PaymentException(ErrorCode.INVALID_STATE_TRANSITION,
                        "No approval record exists for payment " + paymentId));
        PaymentApproval decided = approval.reject(checker, notes, clock.instant());
        approvals.save(decided);
        return toView(applyTransition(payment, PaymentStatus.FAILED, "MAKER_CHECKER_REJECTED", checker));
    }

    private PaymentView createPayment(InitiatePaymentCommand command, String idempotencyKey, String requestHash,
                                      String actor, Instant now) {
        BigDecimal amount = normalizeAmount(command.amount());
        TaxPayment payment = TaxPayment.initiate(
                UUID.randomUUID(),
                generateReference(now),
                command.billingId(),
                command.customerId(),
                command.debitAccountNumber(),
                amount,
                command.currency().toUpperCase(Locale.ROOT),
                now
        );

        TaxPayment saved = taxPayments.save(payment);
        String correlationId = CorrelationIdHolder.getOrGenerate();

        approvals.save(PaymentApproval.pending(UUID.randomUUID(), saved.id(), actor, now));
        statusHistories.append(saved.id(), null, saved.status().name(), "PAYMENT_INITIATED",
                actor, correlationId);
        idempotencyRepository.save(new PaymentIdempotency(
                UUID.randomUUID(),
                idempotencyKey,
                requestHash,
                saved.id(),
                now.plus(IDEMPOTENCY_TTL),
                now
        ));
        auditLogs.append("TAX_PAYMENT", saved.id().toString(), "PAYMENT_INITIATED", actor,
                correlationId, null, snapshot(saved));
        eventPublisher.appendForInitiation(saved, "PAYMENT_INITIATED", correlationId);
        return toView(saved);
    }

    private PaymentView resolveExisting(PaymentIdempotency record, String requestHash) {
        if (!record.requestHash().equals(requestHash)) {
            throw new IdempotencyConflictException();
        }
        TaxPayment payment = taxPayments.findById(record.paymentId())
                .orElseThrow(() -> new PaymentNotFoundException(
                        "Payment referenced by idempotency record was not found"));
        return toView(payment);
    }

    private TaxPayment applyTransition(TaxPayment payment, PaymentStatus targetStatus, String reason, String actor) {
        PaymentStatus fromStatus = payment.status();
        Instant now = clock.instant();
        payment.transitionTo(targetStatus, now);
        TaxPayment saved = taxPayments.save(payment);
        String correlationId = CorrelationIdHolder.getOrGenerate();
        statusHistories.append(saved.id(), fromStatus.name(), targetStatus.name(), reason, actor, correlationId);
        auditLogs.append("TAX_PAYMENT", saved.id().toString(), "PAYMENT_STATUS_CHANGED", actor,
                correlationId, Map.of("status", fromStatus.name()), snapshot(saved));
        eventPublisher.appendForTransition(saved, reason, correlationId);
        return saved;
    }

    private TaxPayment loadPayment(UUID paymentId) {
        return taxPayments.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment " + paymentId + " was not found"));
    }

    private void requireAwaitingApproval(TaxPayment payment) {
        if (payment.status() != PaymentStatus.PENDING_APPROVAL) {
            throw new PaymentException(ErrorCode.INVALID_STATE_TRANSITION,
                    "Payment " + payment.id() + " is not awaiting approval; current status is " + payment.status());
        }
    }

    private void requireCheckerRole(String role) {
        if (role != null && !role.isBlank() && !CHECKER_ROLE.equalsIgnoreCase(role)) {
            throw new PaymentException(ErrorCode.FORBIDDEN, "Only a CHECKER may approve or reject a payment");
        }
    }

    private Map<String, Object> snapshot(TaxPayment payment) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("paymentId", payment.id().toString());
        snapshot.put("reference", payment.reference());
        snapshot.put("billingId", payment.billingId());
        snapshot.put("customerId", payment.customerId());
        snapshot.put("amount", payment.amount().toPlainString());
        snapshot.put("currency", payment.currency());
        snapshot.put("status", payment.status().name());
        snapshot.put("version", payment.version());
        return snapshot;
    }

    private PaymentView toView(TaxPayment payment) {
        return new PaymentView(
                payment.id(),
                payment.reference(),
                payment.billingId(),
                payment.customerId(),
                payment.debitAccountNumber(),
                payment.amount(),
                payment.currency(),
                payment.status(),
                payment.version(),
                payment.createdAt(),
                payment.updatedAt()
        );
    }

    private PaymentHistoryView toHistoryView(PaymentStatusHistory history) {
        return new PaymentHistoryView(
                history.id(),
                history.fromStatus() == null ? null : history.fromStatus().name(),
                history.toStatus().name(),
                history.reason(),
                history.actor(),
                history.correlationId(),
                history.occurredAt()
        );
    }

    private String normalizeActor(String actor) {
        if (actor == null || actor.isBlank()) {
            return "system";
        }
        return actor.trim();
    }

    private BigDecimal normalizeAmount(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0 || amount.scale() > 2) {
            throw new PaymentException(ErrorCode.VALIDATION_ERROR,
                    "Amount must be positive with at most 2 decimal places");
        }
        return amount.setScale(2, RoundingMode.UNNECESSARY);
    }

    private String hash(InitiatePaymentCommand command) {
        String canonical = String.join("|",
                command.billingId(),
                command.customerId(),
                command.debitAccountNumber(),
                normalizeAmount(command.amount()).toPlainString(),
                command.currency().toUpperCase(Locale.ROOT)
        );
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(canonical.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }

    private String generateReference(Instant now) {
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase(Locale.ROOT);
        return "TP" + REFERENCE_DATE.format(now) + suffix;
    }
}
