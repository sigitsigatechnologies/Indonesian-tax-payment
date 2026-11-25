package id.co.taxpay.common.error;

import java.time.Instant;
import java.util.List;

public record ApiError(
        Instant timestamp,
        int status,
        String code,
        String message,
        String path,
        String correlationId,
        List<FieldViolation> details
) {

    public record FieldViolation(String field, String message) {
    }

    public static ApiError of(ErrorCode errorCode, String path, String correlationId) {
        return new ApiError(
                Instant.now(),
                errorCode.httpStatus(),
                errorCode.code(),
                errorCode.defaultMessage(),
                path,
                correlationId,
                List.of()
        );
    }

    public static ApiError of(ErrorCode errorCode, String message, String path, String correlationId) {
        return of(errorCode, message, path, correlationId, List.of());
    }

    public static ApiError of(ErrorCode errorCode, String message, String path, String correlationId,
                              List<FieldViolation> details) {
        return new ApiError(
                Instant.now(),
                errorCode.httpStatus(),
                errorCode.code(),
                message,
                path,
                correlationId,
                details == null ? List.of() : List.copyOf(details)
        );
    }
}
