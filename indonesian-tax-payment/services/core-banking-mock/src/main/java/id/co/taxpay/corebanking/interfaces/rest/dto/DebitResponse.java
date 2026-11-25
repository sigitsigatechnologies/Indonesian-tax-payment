package id.co.taxpay.corebanking.interfaces.rest.dto;

import id.co.taxpay.corebanking.infrastructure.persistence.BankTransactionEntity;

import java.math.BigDecimal;
import java.time.Instant;

public record DebitResponse(
        String debitReference,
        String accountNumber,
        BigDecimal amount,
        String currency,
        String status,
        BigDecimal balanceAfter,
        Instant transactionTime,
        boolean duplicateIgnored,
        String disclaimer
) {
    public static DebitResponse from(BankTransactionEntity entity, boolean duplicateIgnored) {
        return new DebitResponse(
                entity.getTransactionReference(),
                entity.getAccountNumber(),
                entity.getAmount(),
                entity.getCurrency(),
                entity.getStatus(),
                entity.getBalanceAfter(),
                entity.getCompletedAt(),
                duplicateIgnored,
                "MOCK CORE BANKING DATA - not a real bank transaction"
        );
    }
}
