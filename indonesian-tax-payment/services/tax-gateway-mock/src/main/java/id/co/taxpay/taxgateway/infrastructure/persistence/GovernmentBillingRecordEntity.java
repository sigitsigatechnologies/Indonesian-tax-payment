package id.co.taxpay.taxgateway.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "government_billing_records")
@Getter
@Setter
@NoArgsConstructor
public class GovernmentBillingRecordEntity {

    @Id
    private UUID id;

    @Column(name = "billing_id", nullable = false, unique = true, length = 64)
    private String billingId;

    @Column(name = "taxpayer_id", nullable = false, length = 64)
    private String taxpayerId;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "status", nullable = false, length = 32)
    private String status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
