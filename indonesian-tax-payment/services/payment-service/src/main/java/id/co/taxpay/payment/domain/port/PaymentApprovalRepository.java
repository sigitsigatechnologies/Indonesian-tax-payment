package id.co.taxpay.payment.domain.port;

import id.co.taxpay.payment.domain.model.PaymentApproval;

import java.util.Optional;
import java.util.UUID;

public interface PaymentApprovalRepository {

    PaymentApproval save(PaymentApproval approval);

    Optional<PaymentApproval> findByPaymentId(UUID paymentId);
}
