package id.co.taxpay.payment.infrastructure.clients.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record SubmitPaymentResponseDto(
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
        boolean duplicateIgnored
) {
}
