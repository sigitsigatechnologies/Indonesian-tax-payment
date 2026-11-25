package id.co.taxpay.payment.domain.exception;

import id.co.taxpay.common.error.ErrorCode;

import java.util.UUID;

public class ApprovalNotPendingException extends PaymentException {

    public ApprovalNotPendingException(UUID paymentId) {
        super(ErrorCode.INVALID_STATE_TRANSITION,
                "Approval for payment " + paymentId + " has already been decided");
    }
}
