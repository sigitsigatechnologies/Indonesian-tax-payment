package id.co.taxpay.payment.infrastructure.clients.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record ReverseResponseDto(
        String reversalReference,
        String originalTransactionReference,
        String accountNumber,
        BigDecimal amount,
        String currency,
        String status,
        String reason,
        Instant reversedAt,
        boolean duplicateIgnored
) {
}
