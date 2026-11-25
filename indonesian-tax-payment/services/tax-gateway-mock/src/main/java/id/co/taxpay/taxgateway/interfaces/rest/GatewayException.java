package id.co.taxpay.taxgateway.interfaces.rest;

import id.co.taxpay.common.error.ErrorCode;

public class GatewayException extends RuntimeException {

    private final ErrorCode errorCode;

    public GatewayException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ErrorCode errorCode() {
        return errorCode;
    }
}
