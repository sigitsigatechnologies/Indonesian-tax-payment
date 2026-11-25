package id.co.taxpay.payment.domain.port;

import id.co.taxpay.payment.domain.model.TaxPayment;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TaxPaymentRepository {

    TaxPayment save(TaxPayment payment);

    Optional<TaxPayment> findById(UUID paymentId);

    Optional<TaxPayment> findByReference(String reference);

    List<TaxPayment> findByCustomerId(String customerId, int offset, int limit);

    long countByCustomerId(String customerId);
}
