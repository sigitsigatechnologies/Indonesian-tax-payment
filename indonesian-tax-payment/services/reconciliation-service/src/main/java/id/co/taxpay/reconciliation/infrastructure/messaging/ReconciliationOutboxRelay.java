package id.co.taxpay.reconciliation.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.co.taxpay.common.event.Topics;
import id.co.taxpay.reconciliation.infrastructure.persistence.ReconciliationOutboxEntity;
import id.co.taxpay.reconciliation.infrastructure.persistence.SpringDataReconciliationOutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
public class ReconciliationOutboxRelay {

    private final SpringDataReconciliationOutboxRepository repository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    @Scheduled(fixedDelayString = "${taxpay.outbox.poll-ms:1000}")
    @Transactional
    public void publishBatch() {
        List<ReconciliationOutboxEntity> events = repository.findPendingForUpdate(50);
        Instant now = clock.instant();
        for (ReconciliationOutboxEntity event : events) {
            try {
                String message = objectMapper.writeValueAsString(event.getPayload());
                kafkaTemplate.send(event.getTopic(), event.getMessageKey(), message).get(5, TimeUnit.SECONDS);
                event.setStatus("PUBLISHED");
                event.setPublishedAt(now);
                repository.save(event);
            } catch (Exception ex) {
                int attempts = event.getAttempts() + 1;
                if (attempts >= 5) {
                    event.setStatus("FAILED");
                    try {
                        String message = objectMapper.writeValueAsString(event.getPayload());
                        kafkaTemplate.send(Topics.DEAD_LETTER, event.getMessageKey(), message)
                                .get(5, TimeUnit.SECONDS);
                    } catch (Exception dltError) {
                        log.error("Reconciliation DLT publish failed for {}", event.getId(), dltError);
                    }
                    log.warn("Reconciliation outbox event {} moved to FAILED", event.getId());
                } else {
                    event.setNextAttemptAt(now.plusMillis(1000L << Math.min(attempts - 1, 10)));
                }
                event.setAttempts(attempts);
                repository.save(event);
            }
        }
    }
}
