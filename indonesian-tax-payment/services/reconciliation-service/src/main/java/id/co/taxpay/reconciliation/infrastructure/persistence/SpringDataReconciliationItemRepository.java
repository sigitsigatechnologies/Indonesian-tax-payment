package id.co.taxpay.reconciliation.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataReconciliationItemRepository extends JpaRepository<ReconciliationItemEntity, UUID> {

    Optional<ReconciliationItemEntity> findByPaymentId(UUID paymentId);

    List<ReconciliationItemEntity> findByCreatedAtBetween(Instant from, Instant to);
}
