package id.co.taxpay.common.event;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Immutable envelope used for every Kafka event published by the platform.
 * The payload is intentionally generic so each bounded context can own its schema.
 */
public record EventEnvelope<T>(
        UUID eventId,
        String eventType,
        int eventVersion,
        String aggregateId,
        String correlationId,
        String causationId,
        Instant timestamp,
        String source,
        T payload
) {

    public EventEnvelope {
        Objects.requireNonNull(eventId, "eventId must not be null");
        Objects.requireNonNull(eventType, "eventType must not be null");
        Objects.requireNonNull(aggregateId, "aggregateId must not be null");
        Objects.requireNonNull(timestamp, "timestamp must not be null");
        Objects.requireNonNull(source, "source must not be null");
        if (eventVersion < 1) {
            throw new IllegalArgumentException("eventVersion must be >= 1");
        }
    }

    public static <T> EventEnvelope<T> create(
            String eventType,
            int eventVersion,
            String aggregateId,
            String correlationId,
            String causationId,
            String source,
            T payload
    ) {
        return new EventEnvelope<>(
                UUID.randomUUID(),
                eventType,
                eventVersion,
                aggregateId,
                correlationId,
                causationId,
                Instant.now(),
                source,
                payload
        );
    }
}
