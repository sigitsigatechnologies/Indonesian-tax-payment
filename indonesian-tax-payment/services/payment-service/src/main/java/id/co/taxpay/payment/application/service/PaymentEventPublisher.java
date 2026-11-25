package id.co.taxpay.payment.application.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import id.co.taxpay.common.event.EventEnvelope;
import id.co.taxpay.common.event.EventHeaders;
import id.co.taxpay.common.event.EventTypes;
import id.co.taxpay.common.event.ServiceNames;
import id.co.taxpay.common.event.TaxPaymentEventPayload;
import id.co.taxpay.common.event.Topics;
import id.co.taxpay.payment.domain.model.OutboxEvent;
import id.co.taxpay.payment.domain.model.PaymentStatus;
import id.co.taxpay.payment.domain.model.TaxPayment;
import id.co.taxpay.payment.domain.port.OutboxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PaymentEventPublisher {

    private static final String AGGREGATE_TYPE = "TAX_PAYMENT";

    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    public void appendForInitiation(TaxPayment payment, String reason, String correlationId) {
        append(payment, EventTypes.TAX_PAYMENT_INITIATED, Topics.PAYMENT_INITIATED,
                reason, correlationId, null);
    }

    public void appendForTransition(TaxPayment payment, String reason, String correlationId) {
        switch (payment.status()) {
            case APPROVED -> append(payment, EventTypes.TAX_PAYMENT_APPROVED, Topics.PAYMENT_APPROVED,
                    reason, correlationId, null);
            case DEBIT_SUCCESS -> append(payment, EventTypes.TAX_PAYMENT_DEBIT_COMPLETED, Topics.DEBIT_COMPLETED,
                    reason, correlationId, null);
            case GOVERNMENT_PROCESSING -> append(payment, EventTypes.TAX_PAYMENT_GOVERNMENT_SUBMITTED,
                    Topics.GOVERNMENT_SUBMITTED, reason, correlationId, null);
            case SUCCESS -> append(payment, EventTypes.TAX_PAYMENT_COMPLETED, Topics.PAYMENT_COMPLETED,
                    reason, correlationId, null);
            case FAILED -> append(payment, EventTypes.TAX_PAYMENT_FAILED, Topics.PAYMENT_FAILED,
                    reason, correlationId, null);
            case REVERSED -> append(payment, EventTypes.TAX_PAYMENT_REVERSED, Topics.PAYMENT_REVERSED,
                    reason, correlationId, null);
            default -> {
                // Intermediate states are not published as domain events.
            }
        }
    }

    private void append(TaxPayment payment, String eventType, String topic, String reason,
                        String correlationId, String causationId) {
        Instant now = clock.instant();
        TaxPaymentEventPayload payload = new TaxPaymentEventPayload(
                payment.id(),
                payment.reference(),
                payment.billingId(),
                payment.customerId(),
                payment.amount(),
                payment.currency(),
                payment.status().name(),
                reason,
                EventTypes.TAX_PAYMENT_DEBIT_COMPLETED.equals(eventType)
                        ? "DBT-" + payment.id() : null,
                EventTypes.TAX_PAYMENT_GOVERNMENT_SUBMITTED.equals(eventType)
                        || EventTypes.TAX_PAYMENT_COMPLETED.equals(eventType)
                        ? "GOV-" + payment.id() : null,
                now
        );
        EventEnvelope<TaxPaymentEventPayload> envelope = new EventEnvelope<>(
                UUID.randomUUID(),
                eventType,
                EventTypes.VERSION_V1,
                payment.id().toString(),
                correlationId,
                causationId,
                now,
                ServiceNames.PAYMENT_SERVICE,
                payload
        );

        Map<String, Object> headers = new LinkedHashMap<>();
        headers.put(EventHeaders.EVENT_ID, envelope.eventId().toString());
        headers.put(EventHeaders.EVENT_TYPE, eventType);
        headers.put(EventHeaders.EVENT_VERSION, envelope.eventVersion());
        headers.put(EventHeaders.AGGREGATE_ID, envelope.aggregateId());
        headers.put(EventHeaders.CORRELATION_ID, nullToEmpty(correlationId));
        headers.put(EventHeaders.CAUSATION_ID, nullToEmpty(causationId));
        headers.put(EventHeaders.SOURCE, envelope.source());

        outboxRepository.append(new OutboxEvent(
                UUID.randomUUID(),
                AGGREGATE_TYPE,
                payment.id().toString(),
                eventType,
                envelope.eventVersion(),
                topic,
                payment.id().toString(),
                objectMapper.convertValue(envelope, new TypeReference<Map<String, Object>>() {
                }),
                headers,
                "PENDING",
                0,
                now,
                now,
                null
        ));
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
