package id.co.taxpay.corebanking.infrastructure.persistence;

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
@Table(name = "bank_reversal_transactions")
@Getter
@Setter
@NoArgsConstructor
public class BankReversalTransactionEntity {

    @Id
    private UUID id;

    @Column(name = "reversal_reference", nullable = false, unique = true, length = 64)
    private String reversalReference;

    @Column(name = "original_transaction_reference", nullable = false, length = 64)
    private String originalTransactionReference;

    @Column(name = "account_number", nullable = false, length = 34)
    private String accountNumber;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "status", nullable = false, length = 32)
    private String status;

    @Column(name = "reason", length = 255)
    private String reason;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "completed_at")
    private Instant completedAt;
}
