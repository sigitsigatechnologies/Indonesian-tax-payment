package id.co.taxpay.payment.infrastructure.clients.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record GovernmentPaymentResponseDto(
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
}
