package id.co.taxpay.reconciliation.interfaces.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import id.co.taxpay.common.event.EventEnvelope;
import id.co.taxpay.common.event.TaxPaymentEventPayload;
import id.co.taxpay.common.event.Topics;
import id.co.taxpay.reconciliation.application.service.ReconciliationIngestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventConsumer {

    private final ObjectMapper objectMapper;
    private final ReconciliationIngestionService ingestionService;

    @KafkaListener(
            topics = {Topics.DEBIT_COMPLETED, Topics.GOVERNMENT_SUBMITTED,
                    Topics.PAYMENT_COMPLETED, Topics.PAYMENT_FAILED, Topics.PAYMENT_REVERSED},
            groupId = "reconciliation-service",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onPaymentEvent(
            String message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) throws JsonProcessingException {
        try {
            EventEnvelope<TaxPaymentEventPayload> envelope = objectMapper.readValue(
                    message, new TypeReference<EventEnvelope<TaxPaymentEventPayload>>() {
                    });
            ingestionService.ingest(envelope, topic, partition, offset);
        } catch (DataIntegrityViolationException duplicate) {
            log.debug("Duplicate reconciliation delivery ignored for {}-{}@{}", topic, partition, offset);
        }
    }
}
