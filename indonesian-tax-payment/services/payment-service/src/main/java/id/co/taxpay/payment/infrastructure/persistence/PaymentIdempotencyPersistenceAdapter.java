package id.co.taxpay.payment.infrastructure.persistence;

import id.co.taxpay.payment.domain.model.PaymentIdempotency;
import id.co.taxpay.payment.domain.port.PaymentIdempotencyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PaymentIdempotencyPersistenceAdapter implements PaymentIdempotencyRepository {

    private final SpringDataPaymentIdempotencyRepository repository;
    private final PaymentEntityMapper mapper;

    @Override
    public Optional<PaymentIdempotency> findByIdempotencyKey(String idempotencyKey) {
        return repository.findByIdempotencyKey(idempotencyKey).map(mapper::toDomain);
    }

    @Override
    public PaymentIdempotency save(PaymentIdempotency record) {
        PaymentIdempotencyEntity entity = new PaymentIdempotencyEntity();
        entity.setId(record.id());
        entity.setIdempotencyKey(record.idempotencyKey());
        entity.setRequestHash(record.requestHash());
        entity.setPaymentId(record.paymentId());
        entity.setExpiresAt(record.expiresAt());
        entity.setCreatedAt(record.createdAt());
        return mapper.toDomain(repository.saveAndFlush(entity));
    }

    @Override
    public void deleteByIdempotencyKey(String idempotencyKey) {
        repository.deleteByIdempotencyKey(idempotencyKey);
    }
}
