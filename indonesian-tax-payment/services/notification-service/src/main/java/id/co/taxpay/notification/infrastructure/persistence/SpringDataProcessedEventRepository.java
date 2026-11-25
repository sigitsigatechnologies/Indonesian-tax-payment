package id.co.taxpay.notification.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataProcessedEventRepository extends JpaRepository<ProcessedEventEntity, UUID> {

    Optional<ProcessedEventEntity> findByConsumerGroupAndEventId(String consumerGroup, UUID eventId);
}
