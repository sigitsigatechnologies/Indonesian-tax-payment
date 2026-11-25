package id.co.taxpay.reconciliation.interfaces.rest.dto;

import id.co.taxpay.reconciliation.infrastructure.persistence.ReconciliationExceptionEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ReconciliationExceptionResponse(
        UUID exceptionId,
        UUID batchId,
        UUID itemId,
        String exceptionType,
        String description,
        BigDecimal amount,
        String currency,
        String status,
        Instant createdAt,
        Instant resolvedAt,
        String resolvedBy
) {
    public static ReconciliationExceptionResponse from(ReconciliationExceptionEntity entity) {
        return new ReconciliationExceptionResponse(entity.getId(), entity.getBatchId(), entity.getItemId(),
                entity.getExceptionType(), entity.getDescription(), entity.getAmount(), entity.getCurrency(),
                entity.getStatus(), entity.getCreatedAt(), entity.getResolvedAt(), entity.getResolvedBy());
    }
}
