package id.co.taxpay.payment.application.model;

import id.co.taxpay.payment.domain.model.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentView(
        UUID paymentId,
        String reference,
        String billingId,
        String customerId,
        String debitAccountNumber,
        BigDecimal amount,
        String currency,
        PaymentStatus status,
        long version,
        Instant createdAt,
        Instant updatedAt
) {
}
