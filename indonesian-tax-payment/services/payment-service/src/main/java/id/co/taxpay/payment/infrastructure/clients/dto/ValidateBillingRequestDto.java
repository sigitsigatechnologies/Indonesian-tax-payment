package id.co.taxpay.payment.infrastructure.clients.dto;

import java.math.BigDecimal;

public record ValidateBillingRequestDto(
        BigDecimal amount,
        String currency,
        String taxpayerId
) {
    public static ValidateBillingRequestDto of(BigDecimal amount, String currency, String taxpayerId) {
        return new ValidateBillingRequestDto(amount, currency, taxpayerId);
    }
}
