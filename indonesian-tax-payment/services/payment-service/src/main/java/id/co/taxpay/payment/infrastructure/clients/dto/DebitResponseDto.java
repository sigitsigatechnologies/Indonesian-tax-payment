package id.co.taxpay.payment.infrastructure.clients.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record DebitResponseDto(
        String debitReference,
        String accountNumber,
        BigDecimal amount,
        String currency,
        String status,
        BigDecimal balanceAfter,
        Instant transactionTime,
        boolean duplicateIgnored
) {
}
