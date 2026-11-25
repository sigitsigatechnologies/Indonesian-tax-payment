package id.co.taxpay.taxgateway.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "government_tax_payments")
@Getter
@Setter
@NoArgsConstructor
public class GovernmentTaxPaymentEntity {

    @Id
    private UUID id;

    @Column(name = "reference", nullable = false, unique = true, length = 64)
    private String reference;

    @Column(name = "billing_id", nullable = false, length = 64)
    private String billingId;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "status", nullable = false, length = 32)
    private String status;

    @Column(name = "mock_ntb", length = 32)
    private String mockNtb;

    @Column(name = "mock_ntpn", length = 32)
    private String mockNtpn;

    @Column(name = "submitted_at")
    private Instant submittedAt;

    @Column(name = "responded_at")
    private Instant respondedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    private long version;
}
