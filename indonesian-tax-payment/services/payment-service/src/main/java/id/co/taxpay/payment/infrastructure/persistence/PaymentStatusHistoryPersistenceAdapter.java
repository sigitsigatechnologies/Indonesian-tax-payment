package id.co.taxpay.payment.infrastructure.persistence;

import id.co.taxpay.payment.domain.model.PaymentStatusHistory;
import id.co.taxpay.payment.domain.port.PaymentStatusHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class PaymentStatusHistoryPersistenceAdapter implements PaymentStatusHistoryRepository {

    private final SpringDataPaymentStatusHistoryRepository repository;
    private final PaymentEntityMapper mapper;

    @Override
    public PaymentStatusHistory append(UUID paymentId, String fromStatus, String toStatus, String reason,
                                       String actor, String correlationId) {
        PaymentStatusHistoryEntity entity = new PaymentStatusHistoryEntity();
        entity.setId(UUID.randomUUID());
        entity.setPaymentId(paymentId);
        entity.setFromStatus(fromStatus);
        entity.setToStatus(toStatus);
        entity.setReason(reason);
        entity.setActor(actor);
        entity.setCorrelationId(correlationId);
        entity.setOccurredAt(Instant.now());
        return mapper.toDomain(repository.save(entity));
    }

    @Override
    public List<PaymentStatusHistory> findByPaymentId(UUID paymentId) {
        return repository.findByPaymentIdOrderByOccurredAtAsc(paymentId).stream()
                .map(mapper::toDomain)
                .toList();
    }
}
