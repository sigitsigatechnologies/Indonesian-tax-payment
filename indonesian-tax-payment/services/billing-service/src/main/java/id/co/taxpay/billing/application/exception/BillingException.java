package id.co.taxpay.billing.application.exception;

import id.co.taxpay.common.error.ErrorCode;

public class BillingException extends RuntimeException {

    private final ErrorCode errorCode;

    public BillingException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ErrorCode errorCode() {
        return errorCode;
    }
}
