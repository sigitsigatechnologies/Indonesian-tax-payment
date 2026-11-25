package id.co.taxpay.notification.interfaces.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import id.co.taxpay.common.event.EventEnvelope;
import id.co.taxpay.common.event.TaxPaymentEventPayload;
import id.co.taxpay.common.event.Topics;
import id.co.taxpay.notification.application.service.NotificationApplicationService;
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
    private final NotificationApplicationService notificationApplicationService;

    @KafkaListener(
            topics = {Topics.PAYMENT_COMPLETED, Topics.PAYMENT_FAILED, Topics.PAYMENT_REVERSED},
            groupId = "notification-service",
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
            notificationApplicationService.handlePaymentEvent(envelope, topic, partition, offset);
            log.info("Processed payment event {} from {}-{}@{}", envelope.eventId(), topic, partition, offset);
        } catch (DataIntegrityViolationException duplicate) {
            log.debug("Duplicate delivery ignored for {}-{}@{}", topic, partition, offset);
        }
    }
}
