package id.co.taxpay.payment.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataTaxPaymentApprovalRepository extends JpaRepository<TaxPaymentApprovalEntity, UUID> {

    Optional<TaxPaymentApprovalEntity> findByPaymentId(UUID paymentId);
}
