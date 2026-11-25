package id.co.taxpay.payment.infrastructure.clients.dto;

import java.math.BigDecimal;

public record BalanceResponseDto(
        String accountNumber,
        String currency,
        BigDecimal availableBalance,
        BigDecimal ledgerBalance
) {
}
