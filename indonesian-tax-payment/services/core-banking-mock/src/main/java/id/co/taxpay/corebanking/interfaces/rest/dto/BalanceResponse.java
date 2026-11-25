package id.co.taxpay.corebanking.interfaces.rest.dto;

import id.co.taxpay.corebanking.infrastructure.persistence.BankAccountEntity;

import java.math.BigDecimal;
import java.time.Instant;

public record BalanceResponse(
        String accountNumber,
        String currency,
        BigDecimal availableBalance,
        BigDecimal ledgerBalance,
        Instant asOf
) {
    public static BalanceResponse from(BankAccountEntity entity) {
        return new BalanceResponse(entity.getAccountNumber(), entity.getCurrency(),
                entity.getAvailableBalance(), entity.getLedgerBalance(), Instant.now());
    }
}
