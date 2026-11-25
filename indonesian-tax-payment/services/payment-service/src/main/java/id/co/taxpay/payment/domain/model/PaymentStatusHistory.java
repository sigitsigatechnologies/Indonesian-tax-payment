package id.co.taxpay.payment.domain.model;

import java.time.Instant;
import java.util.UUID;

public record PaymentStatusHistory(
        UUID id,
        UUID paymentId,
        PaymentStatus fromStatus,
        PaymentStatus toStatus,
        String reason,
        String actor,
        String correlationId,
        Instant occurredAt
) {
}
