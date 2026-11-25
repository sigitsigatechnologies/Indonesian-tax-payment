package id.co.taxpay.billing.interfaces.rest.dto;

import id.co.taxpay.billing.application.model.BillingValidationResult;
import id.co.taxpay.billing.application.model.BillingView;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record BillingValidationResponse(
        boolean valid,
        String resultCode,
        String message,
        Billing billing
) {
    public record Billing(
            UUID id,
            String billingId,
            String taxpayerId,
            String taxType,
            String taxPeriod,
            BigDecimal principalAmount,
            BigDecimal penaltyAmount,
            BigDecimal totalAmount,
            String currency,
            String status,
            LocalDate dueDate,
            Instant paidAt
    ) {
        public static Billing from(BillingView view) {
            return new Billing(view.id(), view.billingId(), view.taxpayerId(), view.taxType(), view.taxPeriod(),
                    view.principalAmount(), view.penaltyAmount(), view.totalAmount(), view.currency(),
                    view.status(), view.dueDate(), view.paidAt());
        }
    }

    public static BillingValidationResponse from(BillingValidationResult result) {
        return new BillingValidationResponse(
                result.valid(),
                result.resultCode(),
                result.message(),
                Billing.from(result.billing())
        );
    }
}
