package id.co.taxpay.payment.domain.exception;

import id.co.taxpay.common.error.ErrorCode;

public class MissingIdempotencyKeyException extends PaymentException {

    public MissingIdempotencyKeyException() {
        super(ErrorCode.MISSING_IDEMPOTENCY_KEY, "Idempotency-Key header is required");
    }
}
