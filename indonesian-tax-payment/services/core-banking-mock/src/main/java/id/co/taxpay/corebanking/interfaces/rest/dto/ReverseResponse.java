package id.co.taxpay.corebanking.interfaces.rest.dto;

import id.co.taxpay.corebanking.infrastructure.persistence.BankReversalTransactionEntity;

import java.math.BigDecimal;
import java.time.Instant;

public record ReverseResponse(
        String reversalReference,
        String originalTransactionReference,
        String accountNumber,
        BigDecimal amount,
        String currency,
        String status,
        String reason,
        Instant reversedAt,
        boolean duplicateIgnored,
        String disclaimer
) {
    public static ReverseResponse from(BankReversalTransactionEntity entity, boolean duplicateIgnored) {
        return new ReverseResponse(
                entity.getReversalReference(),
                entity.getOriginalTransactionReference(),
                entity.getAccountNumber(),
                entity.getAmount(),
                entity.getCurrency(),
                entity.getStatus(),
                entity.getReason(),
                entity.getCompletedAt(),
                duplicateIgnored,
                "MOCK CORE BANKING DATA - not a real bank reversal"
        );
    }
}
