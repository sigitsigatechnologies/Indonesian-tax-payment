package id.co.taxpay.payment.domain;

import id.co.taxpay.payment.domain.exception.InvalidStateTransitionException;
import id.co.taxpay.payment.domain.model.PaymentStatus;
import id.co.taxpay.payment.domain.service.PaymentStateMachine;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaymentStateMachineTest {

    @Test
    void allowsHappyPathTransitions() {
        assertDoesNotThrow(() -> PaymentStateMachine.assertTransition(PaymentStatus.RECEIVED, PaymentStatus.VALIDATING));
        assertDoesNotThrow(() -> PaymentStateMachine.assertTransition(PaymentStatus.VALIDATING, PaymentStatus.VALIDATED));
        assertDoesNotThrow(() -> PaymentStateMachine.assertTransition(PaymentStatus.VALIDATED, PaymentStatus.PENDING_APPROVAL));
        assertDoesNotThrow(() -> PaymentStateMachine.assertTransition(PaymentStatus.PENDING_APPROVAL, PaymentStatus.APPROVED));
        assertDoesNotThrow(() -> PaymentStateMachine.assertTransition(PaymentStatus.APPROVED, PaymentStatus.DEBIT_PENDING));
        assertDoesNotThrow(() -> PaymentStateMachine.assertTransition(PaymentStatus.DEBIT_PENDING, PaymentStatus.DEBIT_SUCCESS));
        assertDoesNotThrow(() -> PaymentStateMachine.assertTransition(PaymentStatus.DEBIT_SUCCESS,
                PaymentStatus.GOVERNMENT_SUBMISSION_PENDING));
        assertDoesNotThrow(() -> PaymentStateMachine.assertTransition(PaymentStatus.GOVERNMENT_SUBMISSION_PENDING,
                PaymentStatus.GOVERNMENT_PROCESSING));
        assertDoesNotThrow(() -> PaymentStateMachine.assertTransition(PaymentStatus.GOVERNMENT_PROCESSING,
                PaymentStatus.SUCCESS));
    }

    @Test
    void rejectsSkippingValidationAndDebit() {
        assertThrows(InvalidStateTransitionException.class,
                () -> PaymentStateMachine.assertTransition(PaymentStatus.RECEIVED, PaymentStatus.SUCCESS));
        assertThrows(InvalidStateTransitionException.class,
                () -> PaymentStateMachine.assertTransition(PaymentStatus.APPROVED, PaymentStatus.DEBIT_SUCCESS));
        assertThrows(InvalidStateTransitionException.class,
                () -> PaymentStateMachine.assertTransition(PaymentStatus.SUCCESS, PaymentStatus.RECEIVED));
    }

    @Test
    void allowsCompensationOnlyWhereBusinessRulesPermitIt() {
        assertTrue(PaymentStateMachine.canTransition(PaymentStatus.SUCCESS, PaymentStatus.REVERSAL_PENDING));
        assertTrue(PaymentStateMachine.canTransition(PaymentStatus.FAILED, PaymentStatus.REVERSAL_PENDING));
        assertFalse(PaymentStateMachine.canTransition(PaymentStatus.RECEIVED, PaymentStatus.REVERSAL_PENDING));
        assertFalse(PaymentStateMachine.canTransition(PaymentStatus.REVERSED, PaymentStatus.REVERSAL_PENDING));
    }

    @Test
    void unknownStateCanBeResolvedByInquiryOnly() {
        assertTrue(PaymentStateMachine.canTransition(PaymentStatus.UNKNOWN, PaymentStatus.SUCCESS));
        assertTrue(PaymentStateMachine.canTransition(PaymentStatus.UNKNOWN, PaymentStatus.GOVERNMENT_PROCESSING));
        assertFalse(PaymentStateMachine.canTransition(PaymentStatus.UNKNOWN, PaymentStatus.APPROVED));
    }
}
