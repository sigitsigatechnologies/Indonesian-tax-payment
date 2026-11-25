package id.co.taxpay.reconciliation.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataReconciliationBatchRepository extends JpaRepository<ReconciliationBatchEntity, UUID> {

    Optional<ReconciliationBatchEntity> findByBatchReference(String batchReference);

    Optional<ReconciliationBatchEntity> findByBusinessDateAndBatchReferenceStartingWith(LocalDate businessDate, String prefix);
}
