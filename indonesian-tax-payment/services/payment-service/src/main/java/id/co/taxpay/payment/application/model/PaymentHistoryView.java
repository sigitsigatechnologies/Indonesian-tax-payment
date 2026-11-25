package id.co.taxpay.payment.application.model;

import java.time.Instant;
import java.util.UUID;

public record PaymentHistoryView(
        UUID historyId,
        String fromStatus,
        String toStatus,
        String reason,
        String actor,
        String correlationId,
        Instant occurredAt
) {
}
