package id.co.taxpay.corebanking.interfaces.rest.dto;

import id.co.taxpay.corebanking.infrastructure.persistence.BankAccountEntity;

import java.math.BigDecimal;
import java.util.UUID;

public record AccountResponse(
        UUID id,
        String accountNumber,
        String accountName,
        String customerId,
        String currency,
        String status
) {
    public static AccountResponse from(BankAccountEntity entity) {
        return new AccountResponse(entity.getId(), entity.getAccountNumber(), entity.getAccountName(),
                entity.getCustomerId(), entity.getCurrency(), entity.getStatus());
    }
}
