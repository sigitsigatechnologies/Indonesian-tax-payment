package id.co.taxpay.payment.domain.model;

import id.co.taxpay.payment.domain.service.PaymentStateMachine;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Payment aggregate root. Infrastructure entities are mapped to this type so
 * business rules never depend on JPA.
 */
public class TaxPayment {

    private final UUID id;
    private final String reference;
    private final String billingId;
    private final String customerId;
    private final String debitAccountNumber;
    private final BigDecimal amount;
    private final String currency;
    private PaymentStatus status;
    private long version;
    private final Instant createdAt;
    private Instant updatedAt;

    private TaxPayment(UUID id,
                       String reference,
                       String billingId,
                       String customerId,
                       String debitAccountNumber,
                       BigDecimal amount,
                       String currency,
                       PaymentStatus status,
                       long version,
                       Instant createdAt,
                       Instant updatedAt) {
        this.id = Objects.requireNonNull(id);
        this.reference = Objects.requireNonNull(reference);
        this.billingId = Objects.requireNonNull(billingId);
        this.customerId = Objects.requireNonNull(customerId);
        this.debitAccountNumber = Objects.requireNonNull(debitAccountNumber);
        this.amount = Objects.requireNonNull(amount);
        this.currency = Objects.requireNonNull(currency);
        this.status = Objects.requireNonNull(status);
        this.version = version;
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
        if (amount.signum() <= 0) {
            throw new IllegalArgumentException("Payment amount must be positive");
        }
    }

    public static TaxPayment initiate(UUID id,
                                      String reference,
                                      String billingId,
                                      String customerId,
                                      String debitAccountNumber,
                                      BigDecimal amount,
                                      String currency,
                                      Instant now) {
        return new TaxPayment(id, reference, billingId, customerId, debitAccountNumber,
                amount, currency, PaymentStatus.RECEIVED, 0L, now, now);
    }

    public static TaxPayment rehydrate(UUID id,
                                       String reference,
                                       String billingId,
                                       String customerId,
                                       String debitAccountNumber,
                                       BigDecimal amount,
                                       String currency,
                                       PaymentStatus status,
                                       long version,
                                       Instant createdAt,
                                       Instant updatedAt) {
        return new TaxPayment(id, reference, billingId, customerId, debitAccountNumber,
                amount, currency, status, version, createdAt, updatedAt);
    }

    public void transitionTo(PaymentStatus nextStatus, Instant now) {
        PaymentStateMachine.assertTransition(this.status, nextStatus);
        this.status = nextStatus;
        this.updatedAt = Objects.requireNonNull(now);
    }

    public UUID id() {
        return id;
    }

    public String reference() {
        return reference;
    }

    public String billingId() {
        return billingId;
    }

    public String customerId() {
        return customerId;
    }

    public String debitAccountNumber() {
        return debitAccountNumber;
    }

    public BigDecimal amount() {
        return amount;
    }

    public String currency() {
        return currency;
    }

    public PaymentStatus status() {
        return status;
    }

    public long version() {
        return version;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }
}
