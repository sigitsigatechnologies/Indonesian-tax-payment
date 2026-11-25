package id.co.taxpay.reconciliation.application.service;

import id.co.taxpay.common.event.EventTypes;
import id.co.taxpay.common.event.ServiceNames;
import id.co.taxpay.common.event.Topics;
import id.co.taxpay.common.error.ErrorCode;
import id.co.taxpay.reconciliation.infrastructure.persistence.ReconciliationBatchEntity;
import id.co.taxpay.reconciliation.infrastructure.persistence.ReconciliationExceptionEntity;
import id.co.taxpay.reconciliation.infrastructure.persistence.ReconciliationItemEntity;
import id.co.taxpay.reconciliation.infrastructure.persistence.ReconciliationOutboxEntity;
import id.co.taxpay.reconciliation.infrastructure.persistence.SpringDataReconciliationBatchRepository;
import id.co.taxpay.reconciliation.infrastructure.persistence.SpringDataReconciliationExceptionRepository;
import id.co.taxpay.reconciliation.infrastructure.persistence.SpringDataReconciliationItemRepository;
import id.co.taxpay.reconciliation.infrastructure.persistence.SpringDataReconciliationOutboxRepository;
import id.co.taxpay.reconciliation.interfaces.rest.dto.BatchResponse;
import id.co.taxpay.reconciliation.interfaces.rest.dto.ReconciliationExceptionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReconciliationBatchService {

    private static final DateTimeFormatter REFERENCE_TIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final SpringDataReconciliationBatchRepository batchRepository;
    private final SpringDataReconciliationItemRepository itemRepository;
    private final SpringDataReconciliationExceptionRepository exceptionRepository;
    private final SpringDataReconciliationOutboxRepository outboxRepository;
    private final Clock clock;

    @Transactional
    public BatchResponse run(LocalDate businessDate, String actor) {
        Instant start = businessDate.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant end = businessDate.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        List<ReconciliationItemEntity> items = itemRepository.findByCreatedAtBetween(start, end);
        Instant now = clock.instant();

        ReconciliationBatchEntity batch = batchRepository.saveAndFlush(newReconciliationBatch(businessDate, now));
        Set<String> duplicateDebitReferences = findDuplicateDebitReferences(items);
        int exceptions = 0;
        int matched = 0;
        List<ReconciliationExceptionEntity> exceptionEntities = new ArrayList<>();

        for (ReconciliationItemEntity item : items) {
            item.setBatchId(batch.getId());
            String outcome = evaluate(item, duplicateDebitReferences);
            item.setMatchStatus(outcome.equals("MATCHED") ? "MATCHED" : outcome);
            itemRepository.save(item);
            if ("MATCHED".equals(outcome)) {
                matched++;
            } else {
                exceptions++;
                exceptionEntities.add(newException(batch, item, outcome));
            }
        }
        if (!exceptionEntities.isEmpty()) {
            exceptionRepository.saveAll(exceptionEntities);
        }

        batch.setTotalRecords(items.size());
        batch.setMatchedRecords(matched);
        batch.setExceptionRecords(exceptions);
        batch.setStatus("COMPLETED");
        batch.setCompletedAt(now);
        batch.setUpdatedAt(now);
        ReconciliationBatchEntity saved = batchRepository.saveAndFlush(batch);

        appendCompletedEvent(saved, now, actor);
        return BatchResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public BatchResponse getBatch(UUID batchId) {
        return batchRepository.findById(batchId)
                .map(BatchResponse::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Reconciliation batch " + batchId + " was not found"));
    }

    @Transactional(readOnly = true)
    public Page<ReconciliationExceptionResponse> getExceptions(String status, Pageable pageable) {
        return (status == null || status.isBlank())
                ? exceptionRepository.findAll(pageable).map(ReconciliationExceptionResponse::from)
                : exceptionRepository.findByStatus(status, pageable).map(ReconciliationExceptionResponse::from);
    }

    @Transactional
    public ReconciliationExceptionResponse resolve(UUID exceptionId, String actor) {
        ReconciliationExceptionEntity exception = exceptionRepository.findById(exceptionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Reconciliation exception " + exceptionId + " was not found"));
        if (!"OPEN".equals(exception.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Exception has already been resolved");
        }
        exception.setStatus("RESOLVED");
        exception.setResolvedAt(clock.instant());
        exception.setResolvedBy(actor);
        return ReconciliationExceptionResponse.from(exceptionRepository.saveAndFlush(exception));
    }

    private ReconciliationBatchEntity newReconciliationBatch(LocalDate businessDate, Instant now) {
        String reference = "RB-" + LocalDateTime.ofInstant(now, ZoneOffset.UTC).format(REFERENCE_TIME)
                + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        ReconciliationBatchEntity batch = new ReconciliationBatchEntity();
        batch.setId(UUID.randomUUID());
        batch.setBatchReference(reference);
        batch.setBusinessDate(businessDate);
        batch.setStatus("RUNNING");
        batch.setStartedAt(now);
        batch.setCreatedAt(now);
        batch.setUpdatedAt(now);
        batch.setVersion(0L);
        return batch;
    }

    private Set<String> findDuplicateDebitReferences(List<ReconciliationItemEntity> items) {
        Set<String> duplicates = new HashSet<>();
        Set<String> seen = new HashSet<>();
        for (ReconciliationItemEntity item : items) {
            if (item.getDebitReference() == null) {
                continue;
            }
            if (!seen.add(item.getDebitReference())) {
                duplicates.add(item.getDebitReference());
            }
        }
        return duplicates;
    }

    private String evaluate(ReconciliationItemEntity item, Set<String> duplicateDebitReferences) {
        if (item.getDebitReference() != null && duplicateDebitReferences.contains(item.getDebitReference())) {
            return "DUPLICATE";
        }
        if (item.getDebitReference() == null) {
            return "MISSING_DEBIT";
        }
        if (item.getGovernmentReference() == null) {
            return "MISSING_GOVERNMENT";
        }
        BigDecimal difference = BigDecimal.ZERO.setScale(2);
        if (item.getAmount() != null && item.getGovernmentAmount() != null) {
            difference = item.getAmount().subtract(item.getGovernmentAmount()).setScale(2);
        }
        item.setDiscrepancyAmount(difference);
        if (difference.compareTo(BigDecimal.ZERO) != 0) {
            return "AMOUNT_MISMATCH";
        }
        return "MATCHED";
    }

    private ReconciliationExceptionEntity newException(ReconciliationBatchEntity batch,
                                                       ReconciliationItemEntity item, String type) {
        ReconciliationExceptionEntity exception = new ReconciliationExceptionEntity();
        exception.setId(UUID.randomUUID());
        exception.setBatchId(batch.getId());
        exception.setItemId(item.getId());
        exception.setExceptionType(type);
        exception.setDescription(type + " for payment " + item.getPaymentReference());
        exception.setAmount(item.getDiscrepancyAmount().compareTo(BigDecimal.ZERO) != 0
                ? item.getDiscrepancyAmount() : item.getAmount());
        exception.setCurrency(item.getCurrency());
        exception.setStatus("OPEN");
        exception.setCreatedAt(clock.instant());
        return exception;
    }

    private void appendCompletedEvent(ReconciliationBatchEntity batch, Instant now, String actor) {
        Map<String, Object> payload = Map.of(
                "batchId", batch.getId().toString(),
                "batchReference", batch.getBatchReference(),
                "businessDate", batch.getBusinessDate().toString(),
                "totalRecords", batch.getTotalRecords(),
                "matchedRecords", batch.getMatchedRecords(),
                "exceptionRecords", batch.getExceptionRecords(),
                "processedBy", actor
        );
        ReconciliationOutboxEntity event = new ReconciliationOutboxEntity();
        event.setId(UUID.randomUUID());
        event.setAggregateType("RECONCILIATION_BATCH");
        event.setAggregateId(batch.getId().toString());
        event.setEventType(EventTypes.TAX_PAYMENT_RECONCILIATION_COMPLETED);
        event.setEventVersion(1);
        event.setTopic(Topics.RECONCILIATION_COMPLETED);
        event.setMessageKey(batch.getId().toString());
        event.setPayload(payload);
        event.setHeaders(Map.of("source", ServiceNames.RECONCILIATION_SERVICE,
                "correlationId", actor));
        event.setStatus("PENDING");
        event.setAttempts(0);
        event.setNextAttemptAt(now);
        event.setCreatedAt(now);
        outboxRepository.saveAndFlush(event);
    }
}
