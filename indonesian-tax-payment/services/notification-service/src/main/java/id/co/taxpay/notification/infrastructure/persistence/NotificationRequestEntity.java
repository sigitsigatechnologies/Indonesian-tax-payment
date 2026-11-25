package id.co.taxpay.notification.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "notification_requests")
@Getter
@Setter
@NoArgsConstructor
public class NotificationRequestEntity {

    @Id
    private UUID id;

    @Column(name = "notification_id", nullable = false, unique = true, length = 64)
    private String notificationId;

    @Column(name = "payment_id", nullable = false)
    private UUID paymentId;

    @Column(name = "channel", nullable = false, length = 32)
    private String channel;

    @Column(name = "recipient", nullable = false, length = 255)
    private String recipient;

    @Column(name = "template_code", nullable = false, length = 64)
    private String templateCode;

    @Column(name = "status", nullable = false, length = 32)
    private String status;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> payload;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    private long version;
}
