package id.co.taxpay.payment.interfaces.rest.dto;

import id.co.taxpay.payment.application.model.PaymentHistoryView;

import java.time.Instant;
import java.util.UUID;

public record PaymentHistoryResponse(
        UUID historyId,
        String fromStatus,
        String toStatus,
        String reason,
        String actor,
        String correlationId,
        Instant occurredAt
) {
    public static PaymentHistoryResponse from(PaymentHistoryView view) {
        return new PaymentHistoryResponse(
                view.historyId(),
                view.fromStatus(),
                view.toStatus(),
                view.reason(),
                view.actor(),
                view.correlationId(),
                view.occurredAt()
        );
    }
}
