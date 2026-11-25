package id.co.taxpay.notification.application.service;

import id.co.taxpay.common.event.EventEnvelope;
import id.co.taxpay.common.event.EventTypes;
import id.co.taxpay.common.event.TaxPaymentEventPayload;
import id.co.taxpay.notification.infrastructure.persistence.NotificationRequestEntity;
import id.co.taxpay.notification.infrastructure.persistence.ProcessedEventEntity;
import id.co.taxpay.notification.infrastructure.persistence.SpringDataNotificationRequestRepository;
import id.co.taxpay.notification.infrastructure.persistence.SpringDataProcessedEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationApplicationService {

    public static final String CONSUMER_GROUP = "notification-service";

    private final SpringDataNotificationRequestRepository notificationRepository;
    private final SpringDataProcessedEventRepository processedEventRepository;
    private final Clock clock;

    @Transactional
    public void handlePaymentEvent(EventEnvelope<TaxPaymentEventPayload> envelope,
                                   String topic, int partition, long offset) {
        if (processedEventRepository.findByConsumerGroupAndEventId(CONSUMER_GROUP, envelope.eventId()).isPresent()) {
            return;
        }
        TaxPaymentEventPayload payload = envelope.payload();
        if (payload == null || payload.paymentId() == null) {
            throw new IllegalArgumentException("Payment event payload is missing paymentId");
        }
        String templateCode = templateFor(envelope.eventType());
        Instant now = clock.instant();

        NotificationRequestEntity request = new NotificationRequestEntity();
        request.setId(UUID.randomUUID());
        request.setNotificationId("NOTIF-" + envelope.eventId());
        request.setPaymentId(payload.paymentId());
        request.setChannel("WEBHOOK");
        request.setRecipient("customer:" + payload.customerId());
        request.setTemplateCode(templateCode);
        request.setStatus("PENDING");
        request.setPayload(buildPayload(envelope, topic));
        request.setCreatedAt(now);
        request.setUpdatedAt(now);
        request.setVersion(0L);
        notificationRepository.saveAndFlush(request);

        ProcessedEventEntity processed = new ProcessedEventEntity();
        processed.setId(UUID.randomUUID());
        processed.setConsumerGroup(CONSUMER_GROUP);
        processed.setEventId(envelope.eventId());
        processed.setTopic(topic);
        processed.setPartitionNo(partition);
        processed.setOffsetNo(offset);
        processed.setProcessedAt(now);
        processedEventRepository.saveAndFlush(processed);
    }

    private String templateFor(String eventType) {
        return switch (eventType) {
            case EventTypes.TAX_PAYMENT_COMPLETED -> "TAX_PAYMENT_SUCCESS";
            case EventTypes.TAX_PAYMENT_FAILED -> "TAX_PAYMENT_FAILED";
            case EventTypes.TAX_PAYMENT_REVERSED -> "TAX_PAYMENT_REVERSED";
            default -> throw new IllegalArgumentException("Unsupported notification event type: " + eventType);
        };
    }

    private Map<String, Object> buildPayload(EventEnvelope<TaxPaymentEventPayload> envelope, String topic) {
        TaxPaymentEventPayload payment = envelope.payload();
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("eventId", envelope.eventId().toString());
        payload.put("eventType", envelope.eventType());
        payload.put("correlationId", envelope.correlationId());
        payload.put("topic", topic);
        payload.put("paymentId", payment.paymentId().toString());
        payload.put("paymentReference", payment.reference());
        payload.put("billingId", payment.billingId());
        payload.put("amount", payment.amount() == null ? null : payment.amount().toPlainString());
        payload.put("currency", payment.currency());
        payload.put("status", payment.status());
        payload.put("reason", payment.reason());
        return payload;
    }
}
