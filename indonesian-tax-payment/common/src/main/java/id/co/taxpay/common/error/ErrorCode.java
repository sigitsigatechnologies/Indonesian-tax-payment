package id.co.taxpay.common.error;

/**
 * Stable, client-facing error codes. Numeric HTTP status is kept here so every
 * service returns the same contract for the same failure class.
 */
public enum ErrorCode {

    VALIDATION_ERROR("VALIDATION_ERROR", "Request validation failed", 400),
    INVALID_REQUEST("INVALID_REQUEST", "The request is invalid", 400),
    MISSING_IDEMPOTENCY_KEY("MISSING_IDEMPOTENCY_KEY", "Idempotency-Key header is required", 400),
    UNAUTHORIZED("UNAUTHORIZED", "Authentication is required", 401),
    FORBIDDEN("FORBIDDEN", "You are not allowed to perform this action", 403),
    NOT_FOUND("NOT_FOUND", "Resource was not found", 404),
    PAYMENT_NOT_FOUND("PAYMENT_NOT_FOUND", "Payment was not found", 404),
    BILLING_NOT_FOUND("BILLING_NOT_FOUND", "Tax billing was not found", 404),
    ACCOUNT_NOT_FOUND("ACCOUNT_NOT_FOUND", "Bank account was not found", 404),
    GOVERNMENT_PAYMENT_NOT_FOUND("GOVERNMENT_PAYMENT_NOT_FOUND", "Government payment was not found", 404),
    RECONCILIATION_BATCH_NOT_FOUND("RECONCILIATION_BATCH_NOT_FOUND", "Reconciliation batch was not found", 404),
    DUPLICATE_REQUEST("DUPLICATE_REQUEST", "The request has already been processed", 409),
    CONCURRENT_MODIFICATION("CONCURRENT_MODIFICATION", "The resource was modified concurrently; retry the operation", 409),
    IDEMPOTENCY_CONFLICT("IDEMPOTENCY_CONFLICT", "Idempotency key was reused with a different payload", 409),
    INVALID_STATE_TRANSITION("INVALID_STATE_TRANSITION", "The requested state transition is not allowed", 409),
    BILLING_ALREADY_PAID("BILLING_ALREADY_PAID", "Tax billing has already been paid", 409),
    DUPLICATE_DEBIT("DUPLICATE_DEBIT", "A debit with the same reference already exists", 409),
    DEBIT_FAILED("DEBIT_FAILED", "Account debit could not be completed", 422),
    PAYMENT_RESULT_UNKNOWN("PAYMENT_RESULT_UNKNOWN", "External payment result is unknown and requires inquiry", 504),
    BILLING_EXPIRED("BILLING_EXPIRED", "Tax billing has expired", 422),
    BILLING_INACTIVE("BILLING_INACTIVE", "Tax billing is not active", 422),
    AMOUNT_MISMATCH("AMOUNT_MISMATCH", "Tax amount does not match the billing", 422),
    CURRENCY_MISMATCH("CURRENCY_MISMATCH", "Currency does not match the billing", 422),
    TAXPAYER_MISMATCH("TAXPAYER_MISMATCH", "Taxpayer does not match the billing", 422),
    INSUFFICIENT_BALANCE("INSUFFICIENT_BALANCE", "Insufficient available balance", 422),
    ACCOUNT_INACTIVE("ACCOUNT_INACTIVE", "Bank account is not active", 422),
    ACCOUNT_CURRENCY_MISMATCH("ACCOUNT_CURRENCY_MISMATCH", "Bank account currency does not match", 422),
    GOVERNMENT_REJECTED("GOVERNMENT_REJECTED", "Government gateway rejected the payment", 422),
    REVERSAL_NOT_ALLOWED("REVERSAL_NOT_ALLOWED", "Reversal is not allowed for the current payment state", 422),
    UPSTREAM_TIMEOUT("UPSTREAM_TIMEOUT", "Upstream service timed out; result is unknown", 504),
    UPSTREAM_UNAVAILABLE("UPSTREAM_UNAVAILABLE", "Upstream service is unavailable", 503),
    INTERNAL_ERROR("INTERNAL_ERROR", "An unexpected error occurred", 500);

    private final String code;
    private final String defaultMessage;
    private final int httpStatus;

    ErrorCode(String code, String defaultMessage, int httpStatus) {
        this.code = code;
        this.defaultMessage = defaultMessage;
        this.httpStatus = httpStatus;
    }

    public String code() {
        return code;
    }

    public String defaultMessage() {
        return defaultMessage;
    }

    public int httpStatus() {
        return httpStatus;
    }
}
