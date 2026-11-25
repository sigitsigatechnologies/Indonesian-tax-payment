package id.co.taxpay.payment.domain;

import id.co.taxpay.payment.domain.exception.InvalidStateTransitionException;
import id.co.taxpay.payment.domain.model.PaymentStatus;
import id.co.taxpay.payment.domain.model.TaxPayment;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TaxPaymentTest {

    private final Instant now = Instant.parse("2026-11-25T00:00:00Z");

    @Test
    void newPaymentStartsReceivedAndCanBeValidated() {
        TaxPayment payment = newPayment();

        assertEquals(PaymentStatus.RECEIVED, payment.status());
        assertEquals(new BigDecimal("1500000.00"), payment.amount());

        payment.transitionTo(PaymentStatus.VALIDATING, now.plusSeconds(5));
        payment.transitionTo(PaymentStatus.VALIDATED, now.plusSeconds(10));

        assertEquals(PaymentStatus.VALIDATED, payment.status());
        assertEquals(now.plusSeconds(10), payment.updatedAt());
    }

    @Test
    void cannotSkipLifecycleSteps() {
        TaxPayment payment = newPayment();

        assertThrows(InvalidStateTransitionException.class,
                () -> payment.transitionTo(PaymentStatus.SUCCESS, now));
    }

    @Test
    void amountMustBePositive() {
        assertThrows(IllegalArgumentException.class, () -> TaxPayment.initiate(
                UUID.randomUUID(), "TP-1", "BILL-1", "CUST-1", "1234567890",
                BigDecimal.ZERO, "IDR", now));
    }

    private TaxPayment newPayment() {
        return TaxPayment.initiate(
                UUID.randomUUID(),
                "TP20261125TEST000001",
                "BILL-2026-0001",
                "CUST-001",
                "1234567890",
                new BigDecimal("1500000.00"),
                "IDR",
                now
        );
    }
}
