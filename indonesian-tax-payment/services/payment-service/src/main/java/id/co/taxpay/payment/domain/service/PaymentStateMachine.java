package id.co.taxpay.payment.domain.service;

import id.co.taxpay.payment.domain.exception.InvalidStateTransitionException;
import id.co.taxpay.payment.domain.model.PaymentStatus;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import static id.co.taxpay.payment.domain.model.PaymentStatus.APPROVED;
import static id.co.taxpay.payment.domain.model.PaymentStatus.DEBIT_PENDING;
import static id.co.taxpay.payment.domain.model.PaymentStatus.DEBIT_SUCCESS;
import static id.co.taxpay.payment.domain.model.PaymentStatus.EXPIRED;
import static id.co.taxpay.payment.domain.model.PaymentStatus.FAILED;
import static id.co.taxpay.payment.domain.model.PaymentStatus.GOVERNMENT_PROCESSING;
import static id.co.taxpay.payment.domain.model.PaymentStatus.GOVERNMENT_SUBMISSION_PENDING;
import static id.co.taxpay.payment.domain.model.PaymentStatus.PENDING_APPROVAL;
import static id.co.taxpay.payment.domain.model.PaymentStatus.RECEIVED;
import static id.co.taxpay.payment.domain.model.PaymentStatus.REVERSAL_PENDING;
import static id.co.taxpay.payment.domain.model.PaymentStatus.REVERSED;
import static id.co.taxpay.payment.domain.model.PaymentStatus.SUCCESS;
import static id.co.taxpay.payment.domain.model.PaymentStatus.UNKNOWN;
import static id.co.taxpay.payment.domain.model.PaymentStatus.VALIDATED;
import static id.co.taxpay.payment.domain.model.PaymentStatus.VALIDATING;

/**
 * Single source of truth for payment lifecycle transitions. No service may
 * update a payment status without going through this class.
 */
public final class PaymentStateMachine {

    private static final Map<PaymentStatus, Set<PaymentStatus>> ALLOWED = new EnumMap<>(PaymentStatus.class);

    static {
        ALLOWED.put(RECEIVED, EnumSet.of(VALIDATING, FAILED, EXPIRED));
        ALLOWED.put(VALIDATING, EnumSet.of(VALIDATED, FAILED, EXPIRED));
        ALLOWED.put(VALIDATED, EnumSet.of(PENDING_APPROVAL, APPROVED, FAILED));
        ALLOWED.put(PENDING_APPROVAL, EnumSet.of(APPROVED, FAILED, EXPIRED));
        ALLOWED.put(APPROVED, EnumSet.of(DEBIT_PENDING, FAILED));
        ALLOWED.put(DEBIT_PENDING, EnumSet.of(DEBIT_SUCCESS, FAILED, UNKNOWN));
        ALLOWED.put(DEBIT_SUCCESS, EnumSet.of(GOVERNMENT_SUBMISSION_PENDING, REVERSAL_PENDING));
        ALLOWED.put(GOVERNMENT_SUBMISSION_PENDING, EnumSet.of(GOVERNMENT_PROCESSING, FAILED, UNKNOWN));
        ALLOWED.put(GOVERNMENT_PROCESSING, EnumSet.of(SUCCESS, FAILED, UNKNOWN));
        ALLOWED.put(UNKNOWN, EnumSet.of(DEBIT_SUCCESS, GOVERNMENT_PROCESSING, SUCCESS, FAILED, REVERSAL_PENDING));
        ALLOWED.put(SUCCESS, EnumSet.of(REVERSAL_PENDING));
        ALLOWED.put(FAILED, EnumSet.of(REVERSAL_PENDING));
        ALLOWED.put(REVERSAL_PENDING, EnumSet.of(REVERSED, FAILED));
        ALLOWED.put(REVERSED, EnumSet.noneOf(PaymentStatus.class));
        ALLOWED.put(EXPIRED, EnumSet.noneOf(PaymentStatus.class));
    }

    public static boolean canTransition(PaymentStatus from, PaymentStatus to) {
        if (from == null) {
            return to == RECEIVED;
        }
        return ALLOWED.getOrDefault(from, Set.of()).contains(to);
    }

    public static void assertTransition(PaymentStatus from, PaymentStatus to) {
        if (!canTransition(from, to)) {
            throw new InvalidStateTransitionException(from, to);
        }
    }

    public static boolean isTerminal(PaymentStatus status) {
        return status == REVERSED || status == EXPIRED;
    }

    private PaymentStateMachine() {
    }
}
