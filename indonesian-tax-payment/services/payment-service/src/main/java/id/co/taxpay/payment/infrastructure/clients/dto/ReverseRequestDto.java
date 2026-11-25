package id.co.taxpay.payment.infrastructure.clients.dto;

import java.math.BigDecimal;

public record ReverseRequestDto(
        String reversalReference,
        BigDecimal amount,
        String currency,
        String reason
) {
}
