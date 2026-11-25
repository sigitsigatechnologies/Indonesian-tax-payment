package id.co.taxpay.payment.domain.port;

import id.co.taxpay.payment.domain.model.GovernmentPaymentTransaction;

import java.util.Optional;
import java.util.UUID;

public interface GovernmentPaymentRepository {

    GovernmentPaymentTransaction save(GovernmentPaymentTransaction transaction);

    Optional<GovernmentPaymentTransaction> findByPaymentId(UUID paymentId);

    Optional<GovernmentPaymentTransaction> findByReference(String submissionReference);
}
