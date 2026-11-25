package id.co.taxpay.payment.infrastructure.clients.dto;

import java.math.BigDecimal;

public record SubmitPaymentRequestDto(
        String submissionReference,
        String billingId,
        BigDecimal amount,
        String currency,
        String taxpayerId,
        String customerId
) {
}
