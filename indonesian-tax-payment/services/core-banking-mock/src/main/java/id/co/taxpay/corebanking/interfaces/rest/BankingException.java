package id.co.taxpay.corebanking.interfaces.rest;

import id.co.taxpay.common.error.ErrorCode;

public class BankingException extends RuntimeException {

    private final ErrorCode errorCode;

    public BankingException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ErrorCode errorCode() {
        return errorCode;
    }
}
