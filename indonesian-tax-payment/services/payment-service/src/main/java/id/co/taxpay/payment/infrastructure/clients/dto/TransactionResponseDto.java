package id.co.taxpay.payment.infrastructure.clients.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record TransactionResponseDto(
        String transactionReference,
        String accountNumber,
        String transactionType,
        BigDecimal amount,
        String currency,
        String status,
        BigDecimal balanceAfter,
        Instant createdAt,
        Instant completedAt
) {
}
