package id.co.taxpay.payment.domain.port;

import id.co.taxpay.payment.domain.model.PaymentStatusHistory;

import java.util.List;
import java.util.UUID;

public interface PaymentStatusHistoryRepository {

    PaymentStatusHistory append(UUID paymentId, String fromStatus, String toStatus, String reason,
                                 String actor, String correlationId);

    List<PaymentStatusHistory> findByPaymentId(UUID paymentId);
}
