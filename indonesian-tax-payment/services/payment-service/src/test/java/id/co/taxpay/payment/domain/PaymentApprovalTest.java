package id.co.taxpay.payment.domain;

import id.co.taxpay.payment.domain.exception.ApprovalNotPendingException;
import id.co.taxpay.payment.domain.exception.SegregationOfDutiesException;
import id.co.taxpay.payment.domain.model.ApprovalDecision;
import id.co.taxpay.payment.domain.model.PaymentApproval;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaymentApprovalTest {

    private final Instant now = Instant.parse("2026-11-25T00:00:00Z");
    private final UUID paymentId = UUID.randomUUID();

    @Test
    void checkerCanApprovePendingPayment() {
        PaymentApproval pending = PaymentApproval.pending(UUID.randomUUID(), paymentId, "maker.one", now);
        PaymentApproval approved = pending.approve("checker.one", "looks good", now.plusSeconds(60));

        assertTrue(pending.isPending());
        assertFalse(approved.isPending());
        assertEquals(ApprovalDecision.APPROVED, approved.decision());
        assertEquals("checker.one", approved.checkerId());
        assertEquals("looks good", approved.notes());
        assertEquals(now.plusSeconds(60), approved.decidedAt());
    }

    @Test
    void checkerCanRejectPendingPayment() {
        PaymentApproval pending = PaymentApproval.pending(UUID.randomUUID(), paymentId, "maker.one", now);
        PaymentApproval rejected = pending.reject("checker.one", "amount looks wrong", now.plusSeconds(30));

        assertEquals(ApprovalDecision.REJECTED, rejected.decision());
        assertEquals("amount looks wrong", rejected.notes());
    }

    @Test
    void makerCannotApproveOwnPayment() {
        PaymentApproval pending = PaymentApproval.pending(UUID.randomUUID(), paymentId, "maker.one", now);

        SegregationOfDutiesException ex = assertThrows(SegregationOfDutiesException.class,
                () -> pending.approve("maker.one", "self approval", now));
        assertEquals("Maker and checker must be different users", ex.getMessage());
    }

    @Test
    void checkerIdentityIsRequired() {
        PaymentApproval pending = PaymentApproval.pending(UUID.randomUUID(), paymentId, "maker.one", now);

        assertThrows(SegregationOfDutiesException.class,
                () -> pending.approve(" ", "blank checker", now));
    }

    @Test
    void decidedApprovalCannotBeDecidedAgain() {
        PaymentApproval pending = PaymentApproval.pending(UUID.randomUUID(), paymentId, "maker.one", now);
        PaymentApproval approved = pending.approve("checker.one", "ok", now);

        assertThrows(ApprovalNotPendingException.class,
                () -> approved.reject("checker.two", "too late", now.plusSeconds(10)));
    }
}
