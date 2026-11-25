package id.co.taxpay.payment.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.co.taxpay.common.event.Topics;
import id.co.taxpay.payment.domain.model.OutboxEvent;
import id.co.taxpay.payment.domain.port.OutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "taxpay.outbox.enabled", havingValue = "true", matchIfMissing = true)
public class OutboxRelay {

    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    @Value("${taxpay.outbox.batch-size:50}")
    private int batchSize;

    @Value("${taxpay.outbox.max-attempts:5}")
    private int maxAttempts;

    @Value("${taxpay.outbox.base-backoff-ms:1000}")
    private long baseBackoffMs;

    @Value("${taxpay.outbox.max-backoff-ms:300000}")
    private long maxBackoffMs;

    @Value("${taxpay.outbox.send-timeout-seconds:5}")
    private long sendTimeoutSeconds;

    @Scheduled(fixedDelayString = "${taxpay.outbox.poll-ms:1000}")
    @Transactional
    public void publishBatch() {
        List<OutboxEvent> events = outboxRepository.findPendingForUpdate(batchSize);
        if (events.isEmpty()) {
            return;
        }
        Instant now = clock.instant();
        for (OutboxEvent event : events) {
            publishOne(event, now);
        }
    }

    private void publishOne(OutboxEvent event, Instant now) {
        String message;
        try {
            message = objectMapper.writeValueAsString(event.payload());
        } catch (Exception ex) {
            log.error("Cannot serialize outbox event {} to JSON", event.id(), ex);
            outboxRepository.markDead(event.id(), Math.max(event.attempts(), maxAttempts), now);
            return;
        }

        try {
            kafkaTemplate.send(event.topic(), event.messageKey(), message)
                    .get(sendTimeoutSeconds, TimeUnit.SECONDS);
            outboxRepository.markPublished(event.id(), now);
            log.debug("Published outbox event {} to {}", event.id(), event.topic());
        } catch (Exception ex) {
            int attempts = event.attempts() + 1;
            if (attempts >= maxAttempts) {
                sendToDeadLetter(event, message, attempts, now);
                outboxRepository.markDead(event.id(), attempts, now);
                log.warn("Outbox event {} exhausted retries; routed to DLT", event.id(), ex);
            } else {
                long delay = Math.min(maxBackoffMs,
                        baseBackoffMs * (1L << Math.min(attempts - 1, 20)));
                outboxRepository.markFailed(event.id(), attempts, now.plusMillis(delay));
                log.warn("Outbox event {} publish failed; attempt {} scheduled in {} ms",
                        event.id(), attempts, delay, ex);
            }
        }
    }

    private void sendToDeadLetter(OutboxEvent event, String message, int attempts, Instant now) {
        try {
            kafkaTemplate.send(Topics.DEAD_LETTER, event.messageKey(), message)
                    .get(sendTimeoutSeconds, TimeUnit.SECONDS);
            log.warn("Outbox event {} sent to DLT after {} attempts at {}", event.id(), attempts, now);
        } catch (Exception dltError) {
            log.error("Failed to send outbox event {} to DLT", event.id(), dltError);
        }
    }
}
