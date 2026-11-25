package id.co.taxpay.reconciliation.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "reconciliation_batches")
@Getter
@Setter
@NoArgsConstructor
public class ReconciliationBatchEntity {

    @Id
    private UUID id;

    @Column(name = "batch_reference", nullable = false, unique = true, length = 64)
    private String batchReference;

    @Column(name = "business_date", nullable = false)
    private LocalDate businessDate;

    @Column(name = "status", nullable = false, length = 32)
    private String status;

    @Column(name = "total_records", nullable = false)
    private int totalRecords;

    @Column(name = "matched_records", nullable = false)
    private int matchedRecords;

    @Column(name = "exception_records", nullable = false)
    private int exceptionRecords;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    private long version;
}
