package id.co.taxpay.billing.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "billing_validation_histories")
@Getter
@Setter
@NoArgsConstructor
public class BillingValidationHistoryEntity {

    @Id
    private UUID id;

    @Column(name = "billing_id", nullable = false, length = 64)
    private String billingId;

    @Column(name = "validation_result", nullable = false, length = 32)
    private String validationResult;

    @Column(name = "reason", length = 255)
    private String reason;

    @Column(name = "correlation_id", length = 128)
    private String correlationId;

    @Column(name = "validated_at", nullable = false)
    private Instant validatedAt;
}
