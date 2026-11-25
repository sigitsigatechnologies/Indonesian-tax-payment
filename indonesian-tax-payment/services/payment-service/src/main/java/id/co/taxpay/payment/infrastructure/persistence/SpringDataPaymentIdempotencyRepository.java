package id.co.taxpay.payment.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataPaymentIdempotencyRepository extends JpaRepository<PaymentIdempotencyEntity, UUID> {

    Optional<PaymentIdempotencyEntity> findByIdempotencyKey(String idempotencyKey);

    void deleteByIdempotencyKey(String idempotencyKey);
}
