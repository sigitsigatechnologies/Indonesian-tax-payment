package id.co.taxpay.payment.infrastructure.clients.dto;

import java.math.BigDecimal;

public record DebitRequestDto(
        String debitReference,
        String accountNumber,
        BigDecimal amount,
        String currency,
        String description
) {
}
