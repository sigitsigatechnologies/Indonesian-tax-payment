package id.co.taxpay.billing.application.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record BillingView(
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
}
