package id.co.taxpay.corebanking.interfaces.rest;

import id.co.taxpay.common.error.ApiError;
import id.co.taxpay.common.web.CorrelationIdHolder;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class BankingExceptionHandler {

    @ExceptionHandler(BankingException.class)
    public ResponseEntity<ApiError> handleBanking(BankingException ex, HttpServletRequest request) {
        return ResponseEntity.status(ex.errorCode().httpStatus())
                .body(ApiError.of(ex.errorCode(), ex.getMessage(), request.getRequestURI(),
                        CorrelationIdHolder.getOrGenerate()));
    }
}
