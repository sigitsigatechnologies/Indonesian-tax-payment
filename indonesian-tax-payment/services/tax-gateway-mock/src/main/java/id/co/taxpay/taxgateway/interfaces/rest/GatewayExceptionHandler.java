package id.co.taxpay.taxgateway.interfaces.rest;

import id.co.taxpay.common.error.ApiError;
import id.co.taxpay.common.web.CorrelationIdHolder;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GatewayExceptionHandler {

    @ExceptionHandler(GatewayException.class)
    public ResponseEntity<ApiError> handleGateway(GatewayException ex, HttpServletRequest request) {
        return ResponseEntity.status(ex.errorCode().httpStatus())
                .body(ApiError.of(ex.errorCode(), ex.getMessage(), request.getRequestURI(),
                        CorrelationIdHolder.getOrGenerate()));
    }
}
