package id.co.taxpay.billing.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataTaxBillingRepository extends JpaRepository<TaxBillingEntity, UUID> {

    Optional<TaxBillingEntity> findByBillingId(String billingId);
}
