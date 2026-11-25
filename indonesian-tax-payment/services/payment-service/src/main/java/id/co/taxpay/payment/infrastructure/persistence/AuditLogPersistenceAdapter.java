package id.co.taxpay.payment.infrastructure.persistence;

import id.co.taxpay.payment.domain.port.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class AuditLogPersistenceAdapter implements AuditLogRepository {

    private final SpringDataAuditLogRepository repository;

    @Override
    public void append(String aggregateType,
                       String aggregateId,
                       String action,
                       String actor,
                       String correlationId,
                       Map<String, Object> beforeState,
                       Map<String, Object> afterState) {
        AuditLogEntity entity = new AuditLogEntity();
        entity.setId(UUID.randomUUID());
        entity.setAggregateType(aggregateType);
        entity.setAggregateId(aggregateId);
        entity.setAction(action);
        entity.setActor(actor);
        entity.setCorrelationId(correlationId);
        entity.setBeforeState(beforeState);
        entity.setAfterState(afterState);
        entity.setOccurredAt(Instant.now());
        repository.save(entity);
    }
}
