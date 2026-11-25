package id.co.taxpay.payment.infrastructure.persistence;

import id.co.taxpay.payment.domain.model.OutboxEvent;
import id.co.taxpay.payment.domain.port.OutboxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class OutboxEventPersistenceAdapter implements OutboxRepository {

    private final SpringDataOutboxEventRepository repository;
    private final PaymentEntityMapper mapper;

    @Override
    public void append(OutboxEvent event) {
        repository.save(mapper.toEntity(event));
    }

    @Override
    public List<OutboxEvent> findPendingForUpdate(int limit) {
        return repository.findPendingForUpdate(limit).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public void markPublished(UUID eventId, Instant publishedAt) {
        repository.findById(eventId).ifPresent(entity -> {
            entity.setStatus("PUBLISHED");
            entity.setPublishedAt(publishedAt);
            repository.save(entity);
        });
    }

    @Override
    public void markFailed(UUID eventId, int attempts, Instant nextAttemptAt) {
        repository.findById(eventId).ifPresent(entity -> {
            entity.setStatus("PENDING");
            entity.setAttempts(attempts);
            entity.setNextAttemptAt(nextAttemptAt);
            repository.save(entity);
        });
    }

    @Override
    public void markDead(UUID eventId, int attempts, Instant now) {
        repository.findById(eventId).ifPresent(entity -> {
            entity.setStatus("FAILED");
            entity.setAttempts(attempts);
            entity.setNextAttemptAt(now);
            repository.save(entity);
        });
    }
}
