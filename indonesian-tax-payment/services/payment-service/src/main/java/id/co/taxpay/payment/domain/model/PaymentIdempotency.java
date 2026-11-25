package id.co.taxpay.payment.domain.model;

import java.time.Instant;
import java.util.UUID;

public record PaymentIdempotency(
        UUID id,
        String idempotencyKey,
        String requestHash,
        UUID paymentId,
        Instant expiresAt,
        Instant createdAt
) {
}
