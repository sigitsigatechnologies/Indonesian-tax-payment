package id.co.taxpay.payment.domain.model;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record OutboxEvent(
        UUID id,
        String aggregateType,
        String aggregateId,
        String eventType,
        int eventVersion,
        String topic,
        String messageKey,
        Map<String, Object> payload,
        Map<String, Object> headers,
        String status,
        int attempts,
        Instant nextAttemptAt,
        Instant createdAt,
        Instant publishedAt
) {
}
