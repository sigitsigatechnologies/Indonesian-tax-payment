package id.co.taxpay.reconciliation.application.service;

import id.co.taxpay.common.event.EventEnvelope;
import id.co.taxpay.common.event.EventTypes;
import id.co.taxpay.common.event.TaxPaymentEventPayload;
import id.co.taxpay.reconciliation.infrastructure.persistence.ProcessedEventEntity;
import id.co.taxpay.reconciliation.infrastructure.persistence.ReconciliationBatchEntity;
import id.co.taxpay.reconciliation.infrastructure.persistence.ReconciliationItemEntity;
import id.co.taxpay.reconciliation.infrastructure.persistence.SpringDataReconciliationBatchRepository;
import id.co.taxpay.reconciliation.infrastructure.persistence.SpringDataReconciliationItemRepository;
import id.co.taxpay.reconciliation.infrastructure.persistence.SpringDataReconciliationProcessedEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReconciliationIngestionService {

    public static final String CONSUMER_GROUP = "reconciliation-service";

    private final SpringDataReconciliationBatchRepository batchRepository;
    private final SpringDataReconciliationItemRepository itemRepository;
    private final SpringDataReconciliationProcessedEventRepository processedEventRepository;
    private final Clock clock;

    @Transactional
    public void ingest(EventEnvelope<TaxPaymentEventPayload> envelope, String topic,
                       int partition, long offset) {
        if (processedEventRepository.findByConsumerGroupAndEventId(CONSUMER_GROUP, envelope.eventId()).isPresent()) {
            return;
        }
        TaxPaymentEventPayload payload = envelope.payload();
        if (payload == null || payload.paymentId() == null) {
            throw new IllegalArgumentException("Payment event payload is missing paymentId");
        }
        LocalDate businessDate = envelope.timestamp().atZone(ZoneOffset.UTC).toLocalDate();
        ReconciliationBatchEntity batch = ensureStreamingBatch(businessDate);
        ReconciliationItemEntity item = itemRepository.findByPaymentId(payload.paymentId())
                .orElseGet(() -> newItem(batch, payload));

        item.setPaymentReference(payload.reference());
        item.setAmount(payload.amount());
        item.setCurrency(payload.currency());
        switch (envelope.eventType()) {
            case EventTypes.TAX_PAYMENT_DEBIT_COMPLETED ->
                    item.setDebitReference(payload.debitReference());
            case EventTypes.TAX_PAYMENT_GOVERNMENT_SUBMITTED -> {
                item.setGovernmentReference(payload.governmentReference());
                item.setGovernmentAmount(payload.amount());
            }
            case EventTypes.TAX_PAYMENT_COMPLETED -> {
                item.setMatchStatus("MATCHED");
                item.setGovernmentReference(payload.governmentReference());
            }
            default -> item.setMatchStatus("MISSING_GOVERNMENT");
        }
        itemRepository.saveAndFlush(item);

        ProcessedEventEntity processed = new ProcessedEventEntity();
        processed.setId(UUID.randomUUID());
        processed.setConsumerGroup(CONSUMER_GROUP);
        processed.setEventId(envelope.eventId());
        processed.setTopic(topic);
        processed.setPartitionNo(partition);
        processed.setOffsetNo(offset);
        processed.setProcessedAt(clock.instant());
        processedEventRepository.saveAndFlush(processed);
    }

    private ReconciliationBatchEntity ensureStreamingBatch(LocalDate businessDate) {
        return batchRepository
                .findByBusinessDateAndBatchReferenceStartingWith(businessDate, "STREAM-")
                .orElseGet(() -> {
                    Instant now = clock.instant();
                    ReconciliationBatchEntity batch = new ReconciliationBatchEntity();
                    batch.setId(UUID.randomUUID());
                    batch.setBatchReference("STREAM-" + businessDate);
                    batch.setBusinessDate(businessDate);
                    batch.setStatus("RUNNING");
                    batch.setStartedAt(now);
                    batch.setCreatedAt(now);
                    batch.setUpdatedAt(now);
                    batch.setVersion(0L);
                    return batchRepository.saveAndFlush(batch);
                });
    }

    private ReconciliationItemEntity newItem(ReconciliationBatchEntity batch, TaxPaymentEventPayload payload) {
        ReconciliationItemEntity item = new ReconciliationItemEntity();
        item.setId(UUID.randomUUID());
        item.setBatchId(batch.getId());
        item.setPaymentId(payload.paymentId());
        item.setMatchStatus("MISSING_GOVERNMENT");
        item.setDiscrepancyAmount(java.math.BigDecimal.ZERO.setScale(2));
        item.setCreatedAt(clock.instant());
        return item;
    }
}
