package id.co.taxpay.payment.domain.port;

import id.co.taxpay.payment.domain.model.PaymentIdempotency;

import java.util.Optional;

public interface PaymentIdempotencyRepository {

    Optional<PaymentIdempotency> findByIdempotencyKey(String idempotencyKey);

    PaymentIdempotency save(PaymentIdempotency record);

    void deleteByIdempotencyKey(String idempotencyKey);
}
