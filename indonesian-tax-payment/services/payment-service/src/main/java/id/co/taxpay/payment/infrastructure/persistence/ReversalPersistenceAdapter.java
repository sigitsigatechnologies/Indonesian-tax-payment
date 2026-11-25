package id.co.taxpay.payment.infrastructure.persistence;

import id.co.taxpay.payment.domain.model.ReversalTransaction;
import id.co.taxpay.payment.domain.port.ReversalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ReversalPersistenceAdapter implements ReversalRepository {

    private final SpringDataReversalRepository repository;

    @Override
    public ReversalTransaction save(ReversalTransaction transaction) {
        ReversalTransactionEntity entity = new ReversalTransactionEntity();
        entity.setId(transaction.id());
        entity.setPaymentId(transaction.paymentId());
        entity.setDebitTransactionId(transaction.debitTransactionId());
        entity.setReversalReference(transaction.reversalReference());
        entity.setAmount(transaction.amount());
        entity.setCurrency(transaction.currency());
        entity.setStatus(transaction.status());
        entity.setReason(transaction.reason());
        entity.setRequestedAt(transaction.requestedAt());
        entity.setCompletedAt(transaction.completedAt());
        entity.setCreatedAt(transaction.createdAt());
        entity.setUpdatedAt(transaction.updatedAt());
        entity.setVersion(transaction.version());
        return toDomain(repository.saveAndFlush(entity));
    }

    @Override
    public Optional<ReversalTransaction> findByPaymentId(UUID paymentId) {
        return repository.findByPaymentId(paymentId).map(this::toDomain);
    }

    @Override
    public Optional<ReversalTransaction> findByReference(String reversalReference) {
        return repository.findByReversalReference(reversalReference).map(this::toDomain);
    }

    private ReversalTransaction toDomain(ReversalTransactionEntity entity) {
        return new ReversalTransaction(
                entity.getId(), entity.getPaymentId(), entity.getDebitTransactionId(),
                entity.getReversalReference(), entity.getAmount(), entity.getCurrency(), entity.getStatus(),
                entity.getReason(), entity.getRequestedAt(), entity.getCompletedAt(), entity.getCreatedAt(),
                entity.getUpdatedAt(), entity.getVersion()
        );
    }
}
