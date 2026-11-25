package id.co.taxpay.reconciliation.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SpringDataReconciliationExceptionRepository extends JpaRepository<ReconciliationExceptionEntity, UUID> {

    Page<ReconciliationExceptionEntity> findByStatus(String status, Pageable pageable);
}
