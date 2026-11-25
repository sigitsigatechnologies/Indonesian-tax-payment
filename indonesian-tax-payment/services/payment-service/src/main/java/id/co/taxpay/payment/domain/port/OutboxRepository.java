package id.co.taxpay.payment.domain.port;

import id.co.taxpay.payment.domain.model.OutboxEvent;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface OutboxRepository {

    void append(OutboxEvent event);

    List<OutboxEvent> findPendingForUpdate(int limit);

    void markPublished(UUID eventId, Instant publishedAt);

    void markFailed(UUID eventId, int attempts, Instant nextAttemptAt);

    void markDead(UUID eventId, int attempts, Instant now);
}
