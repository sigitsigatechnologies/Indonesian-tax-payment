package id.co.taxpay.payment.domain.model;

import id.co.taxpay.payment.domain.exception.ApprovalNotPendingException;
import id.co.taxpay.payment.domain.exception.SegregationOfDutiesException;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record PaymentApproval(
        UUID id,
        UUID paymentId,
        String makerId,
        String checkerId,
        ApprovalDecision decision,
        String notes,
        Instant decidedAt,
        Instant createdAt
) {

    public PaymentApproval {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(paymentId, "paymentId must not be null");
        Objects.requireNonNull(makerId, "makerId must not be null");
        Objects.requireNonNull(decision, "decision must not be null");
        Objects.requireNonNull(createdAt, "createdAt must not be null");
        if (makerId.isBlank()) {
            throw new IllegalArgumentException("makerId must not be blank");
        }
    }

    public static PaymentApproval pending(UUID id, UUID paymentId, String makerId, Instant now) {
        return new PaymentApproval(id, paymentId, makerId, null, ApprovalDecision.PENDING,
                null, null, now);
    }

    public static PaymentApproval rehydrate(UUID id, UUID paymentId, String makerId, String checkerId,
                                            ApprovalDecision decision, String notes, Instant decidedAt,
                                            Instant createdAt) {
        return new PaymentApproval(id, paymentId, makerId, checkerId, decision, notes, decidedAt, createdAt);
    }

    public boolean isPending() {
        return decision == ApprovalDecision.PENDING;
    }

    public PaymentApproval approve(String checker, String approvalNotes, Instant now) {
        assertPending();
        assertDifferentChecker(checker);
        return new PaymentApproval(id, paymentId, makerId, checker, ApprovalDecision.APPROVED,
                approvalNotes, now, createdAt);
    }

    public PaymentApproval reject(String checker, String rejectionNotes, Instant now) {
        assertPending();
        assertDifferentChecker(checker);
        return new PaymentApproval(id, paymentId, makerId, checker, ApprovalDecision.REJECTED,
                rejectionNotes, now, createdAt);
    }

    private void assertPending() {
        if (!isPending()) {
            throw new ApprovalNotPendingException(paymentId);
        }
    }

    private void assertDifferentChecker(String checker) {
        if (checker == null || checker.isBlank()) {
            throw new SegregationOfDutiesException("Checker identity is required");
        }
        if (checker.equals(makerId)) {
            throw new SegregationOfDutiesException("Maker and checker must be different users");
        }
    }
}
