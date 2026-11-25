package id.co.taxpay.corebanking.interfaces.rest.dto;

import id.co.taxpay.corebanking.infrastructure.persistence.BankTransactionEntity;

import java.math.BigDecimal;
import java.time.Instant;

public record TransactionResponse(
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
    public static TransactionResponse from(BankTransactionEntity entity) {
        return new TransactionResponse(entity.getTransactionReference(), entity.getAccountNumber(),
                entity.getTransactionType(), entity.getAmount(), entity.getCurrency(), entity.getStatus(),
                entity.getBalanceAfter(), entity.getCreatedAt(), entity.getCompletedAt());
    }
}
