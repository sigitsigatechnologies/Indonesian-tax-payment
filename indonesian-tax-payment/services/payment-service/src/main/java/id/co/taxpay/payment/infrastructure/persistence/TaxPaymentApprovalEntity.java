package id.co.taxpay.payment.infrastructure.persistence;

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
@Table(name = "tax_payment_approvals")
@Getter
@Setter
@NoArgsConstructor
public class TaxPaymentApprovalEntity {

    @Id
    private UUID id;

    @Column(name = "payment_id", nullable = false, unique = true)
    private UUID paymentId;

    @Column(name = "maker_id", nullable = false, length = 128)
    private String makerId;

    @Column(name = "checker_id", length = 128)
    private String checkerId;

    @Column(name = "decision", nullable = false, length = 32)
    private String decision;

    @Column(name = "notes", length = 512)
    private String notes;

    @Column(name = "decided_at")
    private Instant decidedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
