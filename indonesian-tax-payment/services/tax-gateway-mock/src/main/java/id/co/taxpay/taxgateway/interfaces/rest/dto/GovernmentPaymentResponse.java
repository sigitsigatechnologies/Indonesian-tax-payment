package id.co.taxpay.taxgateway.interfaces.rest.dto;

import id.co.taxpay.taxgateway.infrastructure.persistence.GovernmentTaxPaymentEntity;

import java.math.BigDecimal;
import java.time.Instant;

public record GovernmentPaymentResponse(
        String reference,
        String billingId,
        BigDecimal amount,
        String currency,
        String status,
        String mockNtb,
        String mockNtpn,
        Instant submittedAt,
        Instant respondedAt,
        String disclaimer
) {
    public static GovernmentPaymentResponse from(GovernmentTaxPaymentEntity entity) {
        return new GovernmentPaymentResponse(entity.getReference(), entity.getBillingId(), entity.getAmount(),
                entity.getCurrency(), entity.getStatus(), entity.getMockNtb(), entity.getMockNtpn(),
                entity.getSubmittedAt(), entity.getRespondedAt(),
                "MOCK DATA - references are simulated and are not issued by DJP/MPN");
    }
}
