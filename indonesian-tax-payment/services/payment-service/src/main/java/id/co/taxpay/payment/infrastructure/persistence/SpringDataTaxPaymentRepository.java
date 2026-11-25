package id.co.taxpay.payment.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataTaxPaymentRepository extends JpaRepository<TaxPaymentEntity, UUID> {

    Optional<TaxPaymentEntity> findByPaymentReference(String paymentReference);

    Page<TaxPaymentEntity> findByCustomerIdOrderByCreatedAtDesc(String customerId, Pageable pageable);
}
