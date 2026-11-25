package id.co.taxpay.payment.domain.exception;

import id.co.taxpay.common.error.ErrorCode;

public class PaymentNotFoundException extends PaymentException {

    public PaymentNotFoundException(String message) {
        super(ErrorCode.PAYMENT_NOT_FOUND, message);
    }
}
