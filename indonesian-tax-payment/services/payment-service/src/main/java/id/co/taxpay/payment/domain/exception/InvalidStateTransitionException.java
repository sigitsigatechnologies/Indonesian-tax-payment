package id.co.taxpay.payment.domain.exception;

import id.co.taxpay.common.error.ErrorCode;
import id.co.taxpay.payment.domain.model.PaymentStatus;

public class InvalidStateTransitionException extends PaymentException {

    public InvalidStateTransitionException(PaymentStatus from, PaymentStatus to) {
        super(ErrorCode.INVALID_STATE_TRANSITION,
                "Transition from " + from + " to " + to + " is not allowed");
    }
}
