package id.co.taxpay.payment.infrastructure.clients.dto;

public record BillingValidationResponseDto(
        boolean valid,
        String resultCode,
        String message,
        Object billing
) {
}
