package id.co.taxpay.payment.application.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import id.co.taxpay.common.error.ErrorCode;
import id.co.taxpay.common.web.CorrelationIdHolder;
import id.co.taxpay.payment.application.model.PaymentView;
import id.co.taxpay.payment.domain.exception.PaymentException;
import id.co.taxpay.payment.domain.model.BankDebitTransaction;
import id.co.taxpay.payment.domain.model.GovernmentPaymentTransaction;
import id.co.taxpay.payment.domain.model.PaymentStatus;
import id.co.taxpay.payment.domain.model.ReversalTransaction;
import id.co.taxpay.payment.domain.model.TaxPayment;
import id.co.taxpay.payment.domain.port.AuditLogRepository;
import id.co.taxpay.payment.domain.port.BankDebitRepository;
import id.co.taxpay.payment.domain.port.GovernmentPaymentRepository;
import id.co.taxpay.payment.domain.port.PaymentStatusHistoryRepository;
import id.co.taxpay.payment.domain.port.ReversalRepository;
import id.co.taxpay.payment.domain.port.TaxPaymentRepository;
import id.co.taxpay.payment.infrastructure.clients.BillingClient;
import id.co.taxpay.payment.infrastructure.clients.CoreBankingClient;
import id.co.taxpay.payment.infrastructure.clients.TaxGatewayClient;
import id.co.taxpay.payment.infrastructure.clients.dto.AccountResponseDto;
import id.co.taxpay.payment.infrastructure.clients.dto.BalanceResponseDto;
import id.co.taxpay.payment.infrastructure.clients.dto.BillingValidationResponseDto;
import id.co.taxpay.payment.infrastructure.clients.dto.DebitRequestDto;
import id.co.taxpay.payment.infrastructure.clients.dto.DebitResponseDto;
import id.co.taxpay.payment.infrastructure.clients.dto.GovernmentPaymentResponseDto;
import id.co.taxpay.payment.infrastructure.clients.dto.ReverseRequestDto;
import id.co.taxpay.payment.infrastructure.clients.dto.ReverseResponseDto;
import id.co.taxpay.payment.infrastructure.clients.dto.SubmitPaymentRequestDto;
import id.co.taxpay.payment.infrastructure.clients.dto.SubmitPaymentResponseDto;
import id.co.taxpay.payment.infrastructure.clients.dto.TransactionResponseDto;
import id.co.taxpay.payment.infrastructure.clients.dto.ValidateBillingRequestDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentSagaOrchestrator {

    private final TaxPaymentRepository taxPayments;
    private final PaymentStatusHistoryRepository statusHistories;
    private final AuditLogRepository auditLogs;
    private final PaymentEventPublisher eventPublisher;
    private final BankDebitRepository debits;
    private final GovernmentPaymentRepository governmentPayments;
    private final ReversalRepository reversals;
    private final BillingClient billingClient;
    private final CoreBankingClient coreBankingClient;
    private final TaxGatewayClient taxGatewayClient;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    @Value("${taxpay.mock.scenario:NONE}")
    private String mockScenario;

    @Async("sagaTaskExecutor")
    public void startAfterInitiation(UUID paymentId) {
        try {
            TaxPayment payment = load(paymentId);
            transition(paymentId, PaymentStatus.VALIDATING, "BILLING_VALIDATION_STARTED", "saga");
            BillingValidationResponseDto validation = validateBilling(
                    payment.billingId(), payment.amount(), payment.currency(), null);
            if (validation == null || !validation.valid()) {
                String reason = validation == null ? ErrorCode.UPSTREAM_UNAVAILABLE.code() : validation.resultCode();
                transition(paymentId, PaymentStatus.FAILED, reason, "saga");
                return;
            }
            transition(paymentId, PaymentStatus.VALIDATED, "BILLING_VALIDATED", "saga");
            transition(paymentId, PaymentStatus.PENDING_APPROVAL, "AWAITING_MAKER_CHECKER", "saga");
        } catch (Exception ex) {
            log.error("Saga validation failed for payment {}", paymentId, ex);
            failSafely(paymentId, "VALIDATION_ERROR");
        }
    }

    @Async("sagaTaskExecutor")
    public void continueAfterApproval(UUID paymentId) {
        try {
            TaxPayment payment = load(paymentId);
            if (payment.status() != PaymentStatus.APPROVED) {
                log.warn("Payment {} is not APPROVED; current status {}", paymentId, payment.status());
                return;
            }
            transition(paymentId, PaymentStatus.DEBIT_PENDING, "ACCOUNT_INQUIRY", "saga");

            AccountResponseDto account = getAccount(payment.debitAccountNumber());
            BalanceResponseDto balance = getBalance(payment.debitAccountNumber());
            if (account == null || balance == null
                    || !"ACTIVE".equals(account.status())
                    || !payment.currency().equals(account.currency())
                    || balance.availableBalance() == null
                    || balance.availableBalance().compareTo(payment.amount()) < 0) {
                transition(paymentId, PaymentStatus.FAILED, "ACCOUNT_INQUIRY_FAILED", "saga");
                return;
            }

            String debitReference = "DBT-" + paymentId;
            BankDebitTransaction debit = ensureDebit(payment, debitReference);
            if (debit == null) {
                transition(paymentId, PaymentStatus.FAILED, ErrorCode.DEBIT_FAILED.code(), "saga");
                return;
            }
            transition(paymentId, PaymentStatus.DEBIT_SUCCESS, "DEBIT_COMPLETED", "saga");
            transition(paymentId, PaymentStatus.GOVERNMENT_SUBMISSION_PENDING, "GOVERNMENT_SUBMISSION", "saga");

            String submissionReference = "GOV-" + paymentId;
            GovernmentPaymentTransaction government = submitToGovernment(payment, submissionReference);
            if (government == null) {
                transition(paymentId, PaymentStatus.FAILED, ErrorCode.PAYMENT_RESULT_UNKNOWN.code(), "saga");
                return;
            }
            if (!"SUCCESS".equals(government.status())) {
                transition(paymentId, PaymentStatus.FAILED, ErrorCode.GOVERNMENT_REJECTED.code(), "saga");
                compensate(paymentId, "GOVERNMENT_REJECTED", "system");
                return;
            }
            transition(paymentId, PaymentStatus.GOVERNMENT_PROCESSING, "GOVERNMENT_ACCEPTED", "saga");
            transition(paymentId, PaymentStatus.SUCCESS, "GOVERNMENT_PAYMENT_CONFIRMED", "saga");
        } catch (Exception ex) {
            log.error("Saga execution failed for payment {}", paymentId, ex);
            failSafely(paymentId, "SAGA_ERROR");
        }
    }

    @Transactional
    public PaymentView reverse(UUID paymentId, String reason, String actor) {
        TaxPayment payment = load(paymentId);
        if (payment.status() != PaymentStatus.SUCCESS && payment.status() != PaymentStatus.FAILED) {
            throw new PaymentException(ErrorCode.REVERSAL_NOT_ALLOWED,
                    "Payment in status " + payment.status() + " cannot be reversed");
        }
        if (payment.status() == PaymentStatus.REVERSED) {
            return toView(payment);
        }
        BankDebitTransaction debit = debits.findByPaymentId(paymentId)
                .orElseThrow(() -> new PaymentException(ErrorCode.REVERSAL_NOT_ALLOWED,
                        "No successful debit exists for payment " + paymentId));
        if (!"COMPLETED".equals(debit.status())) {
            throw new PaymentException(ErrorCode.REVERSAL_NOT_ALLOWED, "Debit is not in COMPLETED status");
        }
        transition(paymentId, PaymentStatus.REVERSAL_PENDING, reason, actor);

        String reversalReference = "REV-" + paymentId;
        Optional<ReversalTransaction> existing = reversals.findByReference(reversalReference);
        if (existing.isEmpty()) {
            ReverseRequestDto request = new ReverseRequestDto(
                    reversalReference, debit.amount(), debit.currency(), reason);
            ReverseResponseDto response = reverseDebit(debit.debitReference(), request);
            boolean success = response != null && "COMPLETED".equals(response.status());
            if (!success) {
                ReverseResponseDto inquiry = getReversal(reversalReference);
                success = inquiry != null && "COMPLETED".equals(inquiry.status());
            }
            if (!success) {
                throw new PaymentException(ErrorCode.UPSTREAM_UNAVAILABLE,
                        "Core banking did not confirm the reversal");
            }
            Instant now = clock.instant();
            reversals.save(new ReversalTransaction(
                    UUID.randomUUID(), paymentId, debit.id(), reversalReference, debit.amount(), debit.currency(),
                    "COMPLETED", reason, now, response == null ? now : response.reversedAt(), now, now, 0L));
        }
        return toView(transition(paymentId, PaymentStatus.REVERSED, reason, actor));
    }

    private void compensate(UUID paymentId, String reason, String actor) {
        TaxPayment payment = load(paymentId);
        if (payment.status() != PaymentStatus.FAILED) {
            return;
        }
        if (debits.findByPaymentId(paymentId).isEmpty()) {
            return;
        }
        try {
            reverse(paymentId, reason, actor);
        } catch (Exception ex) {
            log.error("Compensation failed for payment {}", paymentId, ex);
        }
    }

    private BankDebitTransaction ensureDebit(TaxPayment payment, String debitReference) {
        Optional<BankDebitTransaction> existing = debits.findByReference(debitReference);
        if (existing.isPresent()) {
            return existing.get().status().equals("COMPLETED") ? existing.get() : null;
        }
        DebitRequestDto request = new DebitRequestDto(
                debitReference,
                payment.debitAccountNumber(),
                payment.amount(),
                payment.currency(),
                "Tax payment " + payment.reference()
        );
        DebitResponseDto response = debit(request);
        boolean completed = response != null && "COMPLETED".equals(response.status());
        if (!completed) {
            TransactionResponseDto inquiry = getTransaction(debitReference);
            if (inquiry == null || !"COMPLETED".equals(inquiry.status())) {
                return null;
            }
            response = new DebitResponseDto(
                    inquiry.transactionReference(), inquiry.accountNumber(), inquiry.amount(), inquiry.currency(),
                    inquiry.status(), inquiry.balanceAfter(), inquiry.completedAt(), false);
        }
        Instant now = clock.instant();
        return debits.save(new BankDebitTransaction(
                UUID.randomUUID(), payment.id(), debitReference, payment.debitAccountNumber(),
                payment.amount(), payment.currency(), "COMPLETED", debitReference,
                payload(request), payload(response), response.transactionTime(), now, now, 0L));
    }

    private GovernmentPaymentTransaction submitToGovernment(TaxPayment payment, String submissionReference) {
        Optional<GovernmentPaymentTransaction> existing = governmentPayments.findByReference(submissionReference);
        if (existing.isPresent()) {
            return existing.get();
        }
        SubmitPaymentRequestDto request = new SubmitPaymentRequestDto(
                submissionReference,
                payment.billingId(),
                payment.amount(),
                payment.currency(),
                null,
                payment.customerId()
        );
        SubmitPaymentResponseDto response = submit(request);
        String status = response == null ? null : response.status();
        if (!"SUCCESS".equals(status) && !"REJECTED".equals(status)) {
            GovernmentPaymentResponseDto inquiry = getGovernmentPayment(submissionReference);
            status = inquiry == null ? null : inquiry.status();
        }
        if (!"SUCCESS".equals(status) && !"REJECTED".equals(status)) {
            return null;
        }
        Instant now = clock.instant();
        return governmentPayments.save(new GovernmentPaymentTransaction(
                UUID.randomUUID(), payment.id(), submissionReference, payment.billingId(),
                payment.amount(), payment.currency(), status,
                response == null ? null : response.mockNtb(),
                response == null ? null : response.mockNtpn(),
                payload(request), payload(response),
                response == null ? now : response.processedAt(),
                response == null ? now : response.processedAt(),
                now, now, 0L));
    }

    private TaxPayment transition(UUID paymentId, PaymentStatus target, String reason, String actor) {
        TaxPayment payment = load(paymentId);
        PaymentStatus from = payment.status();
        Instant now = clock.instant();
        payment.transitionTo(target, now);
        TaxPayment saved = taxPayments.save(payment);
        String correlationId = CorrelationIdHolder.getOrGenerate();
        statusHistories.append(saved.id(), from.name(), target.name(), reason, actor, correlationId);
        auditLogs.append("TAX_PAYMENT", saved.id().toString(), "PAYMENT_STATUS_CHANGED", actor,
                correlationId, Map.of("status", from.name()), Map.of("status", target.name(), "reason", reason));
        eventPublisher.appendForTransition(saved, reason, correlationId);
        return saved;
    }

    private void failSafely(UUID paymentId, String reason) {
        try {
            TaxPayment payment = load(paymentId);
            if (!PaymentStateMachineTerminal(payment.status())) {
                transition(paymentId, PaymentStatus.FAILED, reason, "saga");
            }
        } catch (Exception inner) {
            log.error("Could not mark payment {} as FAILED", paymentId, inner);
        }
    }

    private boolean PaymentStateMachineTerminal(PaymentStatus status) {
        return status == PaymentStatus.FAILED || status == PaymentStatus.REVERSED
                || status == PaymentStatus.EXPIRED || status == PaymentStatus.SUCCESS
                || status == PaymentStatus.REVERSAL_PENDING;
    }

    private TaxPayment load(UUID paymentId) {
        return taxPayments.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundExceptionLocal(paymentId));
    }

    private PaymentView toView(TaxPayment payment) {
        return new PaymentView(payment.id(), payment.reference(), payment.billingId(), payment.customerId(),
                payment.debitAccountNumber(), payment.amount(), payment.currency(), payment.status(),
                payment.version(), payment.createdAt(), payment.updatedAt());
    }

    private Map<String, Object> payload(Object value) {
        if (value == null) {
            return Map.of();
        }
        return objectMapper.convertValue(value, new TypeReference<Map<String, Object>>() {
        });
    }

    @CircuitBreaker(name = "billing", fallbackMethod = "validateBillingFallback")
    @Retry(name = "billing")
    public BillingValidationResponseDto validateBilling(String billingId, BigDecimal amount,
                                                        String currency, String taxpayerId) {
        return billingClient.validate(billingId, ValidateBillingRequestDto.of(amount, currency, taxpayerId));
    }

    public BillingValidationResponseDto validateBillingFallback(String billingId, BigDecimal amount,
                                                                String currency, String taxpayerId, Throwable ex) {
        log.warn("Billing validation fallback for {}", billingId, ex);
        return new BillingValidationResponseDto(false, ErrorCode.UPSTREAM_UNAVAILABLE.code(),
                "Billing service unavailable", null);
    }

    @CircuitBreaker(name = "coreBanking", fallbackMethod = "getAccountFallback")
    @Retry(name = "coreBanking")
    public AccountResponseDto getAccount(String accountNumber) {
        return coreBankingClient.getAccount(accountNumber);
    }

    public AccountResponseDto getAccountFallback(String accountNumber, Throwable ex) {
        log.warn("Account inquiry fallback for {}", accountNumber, ex);
        return null;
    }

    @CircuitBreaker(name = "coreBanking", fallbackMethod = "getBalanceFallback")
    @Retry(name = "coreBanking")
    public BalanceResponseDto getBalance(String accountNumber) {
        return coreBankingClient.getBalance(accountNumber);
    }

    public BalanceResponseDto getBalanceFallback(String accountNumber, Throwable ex) {
        log.warn("Balance inquiry fallback for {}", accountNumber, ex);
        return null;
    }

    @CircuitBreaker(name = "coreBanking", fallbackMethod = "debitFallback")
    @Retry(name = "coreBanking")
    public DebitResponseDto debit(DebitRequestDto request) {
        return coreBankingClient.debit(request, mockScenario);
    }

    public DebitResponseDto debitFallback(DebitRequestDto request, Throwable ex) {
        log.warn("Debit fallback for {} - result is UNKNOWN, performing inquiry", request.debitReference(), ex);
        return new DebitResponseDto(request.debitReference(), request.accountNumber(), request.amount(),
                request.currency(), "FAILED", null, null, false);
    }

    @CircuitBreaker(name = "coreBanking", fallbackMethod = "getTransactionFallback")
    @Retry(name = "coreBanking")
    public TransactionResponseDto getTransaction(String reference) {
        return coreBankingClient.getTransaction(reference);
    }

    public TransactionResponseDto getTransactionFallback(String reference, Throwable ex) {
        log.warn("Transaction inquiry fallback for {}", reference, ex);
        return null;
    }

    @CircuitBreaker(name = "coreBanking", fallbackMethod = "reverseDebitFallback")
    @Retry(name = "coreBanking")
    public ReverseResponseDto reverseDebit(String reference, ReverseRequestDto request) {
        return coreBankingClient.reverse(reference, request, mockScenario);
    }

    public ReverseResponseDto reverseDebitFallback(String reference, ReverseRequestDto request, Throwable ex) {
        log.warn("Reversal fallback for {}", reference, ex);
        return new ReverseResponseDto(request.reversalReference(), reference, null, request.amount(),
                request.currency(), "FAILED", request.reason(), null, false);
    }

    @CircuitBreaker(name = "coreBanking", fallbackMethod = "getReversalFallback")
    @Retry(name = "coreBanking")
    public ReverseResponseDto getReversal(String reference) {
        return coreBankingClient.getReversal(reference);
    }

    public ReverseResponseDto getReversalFallback(String reference, Throwable ex) {
        log.warn("Reversal inquiry fallback for {}", reference, ex);
        return null;
    }

    @CircuitBreaker(name = "taxGateway", fallbackMethod = "submitFallback")
    @Retry(name = "taxGateway")
    public SubmitPaymentResponseDto submit(SubmitPaymentRequestDto request) {
        return taxGatewayClient.submit(request, mockScenario);
    }

    public SubmitPaymentResponseDto submitFallback(SubmitPaymentRequestDto request, Throwable ex) {
        log.warn("Government submission fallback for {} - result is UNKNOWN, performing inquiry",
                request.submissionReference(), ex);
        return new SubmitPaymentResponseDto(request.submissionReference(), request.billingId(), request.amount(),
                request.currency(), "FAILED", ErrorCode.UPSTREAM_TIMEOUT.code(),
                "Government submission result unknown", null, null, null, false);
    }

    @CircuitBreaker(name = "taxGateway", fallbackMethod = "getGovernmentPaymentFallback")
    @Retry(name = "taxGateway")
    public GovernmentPaymentResponseDto getGovernmentPayment(String reference) {
        return taxGatewayClient.getPayment(reference);
    }

    public GovernmentPaymentResponseDto getGovernmentPaymentFallback(String reference, Throwable ex) {
        log.warn("Government inquiry fallback for {}", reference, ex);
        return null;
    }

    private static class PaymentNotFoundExceptionLocal extends RuntimeException {
        PaymentNotFoundExceptionLocal(UUID paymentId) {
            super("Payment " + paymentId + " was not found");
        }
    }
}
