package id.co.taxpay.taxgateway.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataGovernmentBillingRepository extends JpaRepository<GovernmentBillingRecordEntity, UUID> {

    Optional<GovernmentBillingRecordEntity> findByBillingId(String billingId);
}
