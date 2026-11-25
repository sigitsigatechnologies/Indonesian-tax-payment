package id.co.taxpay.reconciliation.interfaces.rest.dto;

import id.co.taxpay.reconciliation.infrastructure.persistence.ReconciliationBatchEntity;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record BatchResponse(
        UUID batchId,
        String batchReference,
        LocalDate businessDate,
        String status,
        int totalRecords,
        int matchedRecords,
        int exceptionRecords,
        Instant startedAt,
        Instant completedAt
) {
    public static BatchResponse from(ReconciliationBatchEntity entity) {
        return new BatchResponse(entity.getId(), entity.getBatchReference(), entity.getBusinessDate(),
                entity.getStatus(), entity.getTotalRecords(), entity.getMatchedRecords(),
                entity.getExceptionRecords(), entity.getStartedAt(), entity.getCompletedAt());
    }
}
