package id.co.taxpay.payment.infrastructure.persistence;

import id.co.taxpay.payment.domain.model.PaymentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "tax_payments")
@Getter
@Setter
@NoArgsConstructor
public class TaxPaymentEntity {

    @Id
    private UUID id;

    @Column(name = "payment_reference", nullable = false, unique = true, length = 40)
    private String paymentReference;

    @Column(name = "billing_id", nullable = false, length = 64)
    private String billingId;

    @Column(name = "customer_id", nullable = false, length = 64)
    private String customerId;

    @Column(name = "debit_account_number", nullable = false, length = 34)
    private String debitAccountNumber;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 40)
    private PaymentStatus status;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
