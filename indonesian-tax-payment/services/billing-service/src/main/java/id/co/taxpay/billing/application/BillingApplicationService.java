package id.co.taxpay.billing.application;

import id.co.taxpay.billing.application.exception.BillingNotFoundException;
import id.co.taxpay.billing.application.model.BillingValidationResult;
import id.co.taxpay.billing.application.model.BillingView;
import id.co.taxpay.billing.infrastructure.persistence.BillingValidationHistoryEntity;
import id.co.taxpay.billing.infrastructure.persistence.SpringDataBillingValidationHistoryRepository;
import id.co.taxpay.billing.infrastructure.persistence.SpringDataTaxBillingRepository;
import id.co.taxpay.billing.infrastructure.persistence.TaxBillingEntity;
import id.co.taxpay.common.error.ErrorCode;
import id.co.taxpay.common.web.CorrelationIdHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BillingApplicationService {

    private final SpringDataTaxBillingRepository billingRepository;
    private final SpringDataBillingValidationHistoryRepository validationHistoryRepository;
    private final Clock clock;

    @Transactional(readOnly = true)
    public BillingView getBilling(String billingId) {
        return toView(findBilling(billingId));
    }

    @Transactional
    public BillingValidationResult validate(String billingId, BigDecimal amount, String currency, String taxpayerId) {
        TaxBillingEntity billing = findBilling(billingId);
        BillingView view = toView(billing);
        BillingValidationResult result = evaluate(billing, amount, currency, taxpayerId, view);
        recordHistory(billingId, result);
        return result;
    }

    private BillingValidationResult evaluate(TaxBillingEntity billing, BigDecimal amount, String currency,
                                             String taxpayerId, BillingView view) {
        if (!"ACTIVE".equals(billing.getStatus())) {
            return BillingValidationResult.invalid(ErrorCode.BILLING_INACTIVE.code(),
                    "Billing status is " + billing.getStatus(), view);
        }
        if (billing.getDueDate().isBefore(LocalDate.now(clock))) {
            return BillingValidationResult.invalid(ErrorCode.BILLING_EXPIRED.code(),
                    "Billing expired on " + billing.getDueDate(), view);
        }
        if (billing.getPaidAt() != null) {
            return BillingValidationResult.invalid(ErrorCode.BILLING_ALREADY_PAID.code(),
                    "Billing was already paid", view);
        }
        if (taxpayerId != null && !taxpayerId.isBlank()
                && !billing.getTaxpayerId().equals(taxpayerId)) {
            return BillingValidationResult.invalid(ErrorCode.TAXPAYER_MISMATCH.code(),
                    "Taxpayer does not own this billing", view);
        }
        if (amount != null && billing.getTotalAmount().compareTo(amount) != 0) {
            return BillingValidationResult.invalid(ErrorCode.AMOUNT_MISMATCH.code(),
                    "Payment amount does not match billing total", view);
        }
        if (currency != null && !currency.isBlank()
                && !billing.getCurrency().equals(currency.toUpperCase(Locale.ROOT))) {
            return BillingValidationResult.invalid(ErrorCode.CURRENCY_MISMATCH.code(),
                    "Payment currency does not match billing currency", view);
        }
        return BillingValidationResult.valid(view);
    }

    private void recordHistory(String billingId, BillingValidationResult result) {
        BillingValidationHistoryEntity history = new BillingValidationHistoryEntity();
        history.setId(UUID.randomUUID());
        history.setBillingId(billingId);
        history.setValidationResult(result.valid() ? "VALID" : "INVALID");
        history.setReason(result.message());
        history.setCorrelationId(CorrelationIdHolder.getOrGenerate());
        history.setValidatedAt(Instant.now(clock));
        validationHistoryRepository.save(history);
    }

    private TaxBillingEntity findBilling(String billingId) {
        return billingRepository.findByBillingId(billingId)
                .orElseThrow(() -> new BillingNotFoundException(billingId));
    }

    private BillingView toView(TaxBillingEntity billing) {
        return new BillingView(
                billing.getId(),
                billing.getBillingId(),
                billing.getTaxpayerId(),
                billing.getTaxType(),
                billing.getTaxPeriod(),
                billing.getPrincipalAmount(),
                billing.getPenaltyAmount(),
                billing.getTotalAmount(),
                billing.getCurrency(),
                billing.getStatus(),
                billing.getDueDate(),
                billing.getPaidAt()
        );
    }
}
