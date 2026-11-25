package id.co.taxpay.taxgateway.interfaces.rest.dto;

import id.co.taxpay.taxgateway.infrastructure.persistence.GovernmentTaxPaymentEntity;

import java.math.BigDecimal;
import java.time.Instant;

public record SubmitPaymentResponse(
        String submissionReference,
        String billingId,
        BigDecimal amount,
        String currency,
        String status,
        String responseCode,
        String responseMessage,
        String mockNtb,
        String mockNtpn,
        Instant processedAt,
        boolean duplicateIgnored,
        String disclaimer
) {
    public static SubmitPaymentResponse from(GovernmentTaxPaymentEntity entity, String responseCode,
                                             String responseMessage, boolean duplicateIgnored) {
        return new SubmitPaymentResponse(
                entity.getReference(),
                entity.getBillingId(),
                entity.getAmount(),
                entity.getCurrency(),
                entity.getStatus(),
                responseCode,
                responseMessage,
                entity.getMockNtb(),
                entity.getMockNtpn(),
                entity.getRespondedAt(),
                duplicateIgnored,
                "MOCK DATA - NTB/NTPN values are simulated and are not issued by DJP/MPN"
        );
    }
}
