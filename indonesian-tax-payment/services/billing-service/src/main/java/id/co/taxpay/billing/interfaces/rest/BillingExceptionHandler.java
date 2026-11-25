package id.co.taxpay.billing.interfaces.rest;

import id.co.taxpay.billing.application.exception.BillingException;
import id.co.taxpay.common.error.ApiError;
import id.co.taxpay.common.error.ErrorCode;
import id.co.taxpay.common.web.CorrelationIdHolder;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class BillingExceptionHandler {

    @ExceptionHandler(BillingException.class)
    public ResponseEntity<ApiError> handleBilling(BillingException ex, HttpServletRequest request) {
        return ResponseEntity.status(ex.errorCode().httpStatus())
                .body(ApiError.of(ex.errorCode(), ex.getMessage(), request.getRequestURI(),
                        CorrelationIdHolder.getOrGenerate()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<ApiError.FieldViolation> details = ex.getBindingResult().getFieldErrors().stream()
                .map(this::toViolation)
                .toList();
        return ResponseEntity.status(ErrorCode.VALIDATION_ERROR.httpStatus())
                .body(ApiError.of(ErrorCode.VALIDATION_ERROR, ErrorCode.VALIDATION_ERROR.defaultMessage(),
                        request.getRequestURI(), CorrelationIdHolder.getOrGenerate(), details));
    }

    private ApiError.FieldViolation toViolation(FieldError fieldError) {
        return new ApiError.FieldViolation(fieldError.getField(), fieldError.getDefaultMessage());
    }
}
