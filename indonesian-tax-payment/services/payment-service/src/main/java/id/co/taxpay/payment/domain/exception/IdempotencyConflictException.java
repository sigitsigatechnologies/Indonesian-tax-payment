package id.co.taxpay.payment.domain.exception;

import id.co.taxpay.common.error.ErrorCode;

public class IdempotencyConflictException extends PaymentException {

    public IdempotencyConflictException() {
        super(ErrorCode.IDEMPOTENCY_CONFLICT,
                "The Idempotency-Key was already used for a different payment payload");
    }
}
