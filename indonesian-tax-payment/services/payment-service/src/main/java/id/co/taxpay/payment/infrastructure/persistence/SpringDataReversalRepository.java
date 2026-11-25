package id.co.taxpay.payment.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataReversalRepository extends JpaRepository<ReversalTransactionEntity, UUID> {

    Optional<ReversalTransactionEntity> findByPaymentId(UUID paymentId);

    Optional<ReversalTransactionEntity> findByReversalReference(String reversalReference);
}
