package id.co.taxpay.taxgateway.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SpringDataGovernmentPaymentResponseRepository extends JpaRepository<GovernmentPaymentResponseEntity, UUID> {
}
