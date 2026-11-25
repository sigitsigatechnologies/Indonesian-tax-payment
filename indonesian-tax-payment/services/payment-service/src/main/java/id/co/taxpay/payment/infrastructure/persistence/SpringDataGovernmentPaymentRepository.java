package id.co.taxpay.payment.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataGovernmentPaymentRepository extends JpaRepository<GovernmentPaymentTransactionEntity, UUID> {

    Optional<GovernmentPaymentTransactionEntity> findByPaymentId(UUID paymentId);

    Optional<GovernmentPaymentTransactionEntity> findBySubmissionReference(String submissionReference);
}
