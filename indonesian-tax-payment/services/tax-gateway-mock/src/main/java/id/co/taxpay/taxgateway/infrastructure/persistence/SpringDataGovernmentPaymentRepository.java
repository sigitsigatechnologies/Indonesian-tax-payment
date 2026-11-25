package id.co.taxpay.taxgateway.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataGovernmentPaymentRepository extends JpaRepository<GovernmentTaxPaymentEntity, UUID> {

    Optional<GovernmentTaxPaymentEntity> findByReference(String reference);
}
