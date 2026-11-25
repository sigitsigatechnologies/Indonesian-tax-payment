package id.co.taxpay.payment.infrastructure.clients.dto;

public record AccountResponseDto(
        String accountNumber,
        String accountName,
        String customerId,
        String currency,
        String status
) {
}
