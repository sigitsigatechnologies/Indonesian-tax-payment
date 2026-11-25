package id.co.taxpay.billing.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SpringDataBillingValidationHistoryRepository extends JpaRepository<BillingValidationHistoryEntity, UUID> {
}
