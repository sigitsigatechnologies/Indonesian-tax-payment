package id.co.taxpay.reconciliation.infrastructure.persistence;

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
@Table(name = "reconciliation_items")
@Getter
@Setter
@NoArgsConstructor
public class ReconciliationItemEntity {

    @Id
    private UUID id;

    @Column(name = "batch_id", nullable = false)
    private UUID batchId;

    @Column(name = "payment_id")
    private UUID paymentId;

    @Column(name = "payment_reference", length = 64)
    private String paymentReference;

    @Column(name = "debit_reference", length = 64)
    private String debitReference;

    @Column(name = "government_reference", length = 64)
    private String governmentReference;

    @Column(name = "amount", precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "government_amount", precision = 19, scale = 2)
    private BigDecimal governmentAmount;

    @Column(name = "currency", length = 3)
    private String currency;

    @Column(name = "match_status", nullable = false, length = 32)
    private String matchStatus;

    @Column(name = "discrepancy_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal discrepancyAmount;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
