package id.co.taxpay.notification.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SpringDataNotificationRequestRepository extends JpaRepository<NotificationRequestEntity, UUID> {
}
