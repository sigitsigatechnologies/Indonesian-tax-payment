package id.co.taxpay.payment.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SpringDataPaymentStatusHistoryRepository extends JpaRepository<PaymentStatusHistoryEntity, UUID> {

    List<PaymentStatusHistoryEntity> findByPaymentIdOrderByOccurredAtAsc(UUID paymentId);
}
