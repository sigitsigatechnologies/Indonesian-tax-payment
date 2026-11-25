package id.co.taxpay.payment.domain.port;

import id.co.taxpay.payment.domain.model.ReversalTransaction;

import java.util.Optional;
import java.util.UUID;

public interface ReversalRepository {

    ReversalTransaction save(ReversalTransaction transaction);

    Optional<ReversalTransaction> findByPaymentId(UUID paymentId);

    Optional<ReversalTransaction> findByReference(String reversalReference);
}
