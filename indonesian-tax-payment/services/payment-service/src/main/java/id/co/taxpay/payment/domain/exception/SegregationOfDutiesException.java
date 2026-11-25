package id.co.taxpay.payment.domain.exception;

import id.co.taxpay.common.error.ErrorCode;

public class SegregationOfDutiesException extends PaymentException {

    public SegregationOfDutiesException(String message) {
        super(ErrorCode.FORBIDDEN, message);
    }
}
