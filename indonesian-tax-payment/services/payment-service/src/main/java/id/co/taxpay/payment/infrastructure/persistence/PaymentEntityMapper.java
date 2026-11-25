package id.co.taxpay.payment.infrastructure.persistence;

import id.co.taxpay.payment.domain.model.ApprovalDecision;
import id.co.taxpay.payment.domain.model.OutboxEvent;
import id.co.taxpay.payment.domain.model.PaymentApproval;
import id.co.taxpay.payment.domain.model.PaymentIdempotency;
import id.co.taxpay.payment.domain.model.PaymentStatusHistory;
import id.co.taxpay.payment.domain.model.TaxPayment;
import org.springframework.stereotype.Component;

@Component
public class PaymentEntityMapper {

    public TaxPaymentEntity toEntity(TaxPayment payment) {
        TaxPaymentEntity entity = new TaxPaymentEntity();
        entity.setId(payment.id());
        entity.setPaymentReference(payment.reference());
        entity.setBillingId(payment.billingId());
        entity.setCustomerId(payment.customerId());
        entity.setDebitAccountNumber(payment.debitAccountNumber());
        entity.setAmount(payment.amount());
        entity.setCurrency(payment.currency());
        entity.setStatus(payment.status());
        entity.setVersion(payment.version());
        entity.setCreatedAt(payment.createdAt());
        entity.setUpdatedAt(payment.updatedAt());
        return entity;
    }

    public TaxPayment toDomain(TaxPaymentEntity entity) {
        return TaxPayment.rehydrate(
                entity.getId(),
                entity.getPaymentReference(),
                entity.getBillingId(),
                entity.getCustomerId(),
                entity.getDebitAccountNumber(),
                entity.getAmount(),
                entity.getCurrency(),
                entity.getStatus(),
                entity.getVersion(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public PaymentStatusHistory toDomain(PaymentStatusHistoryEntity entity) {
        return new PaymentStatusHistory(
                entity.getId(),
                entity.getPaymentId(),
                entity.getFromStatus() == null ? null : id.co.taxpay.payment.domain.model.PaymentStatus.valueOf(entity.getFromStatus()),
                id.co.taxpay.payment.domain.model.PaymentStatus.valueOf(entity.getToStatus()),
                entity.getReason(),
                entity.getActor(),
                entity.getCorrelationId(),
                entity.getOccurredAt()
        );
    }

    public PaymentIdempotency toDomain(PaymentIdempotencyEntity entity) {
        return new PaymentIdempotency(
                entity.getId(),
                entity.getIdempotencyKey(),
                entity.getRequestHash(),
                entity.getPaymentId(),
                entity.getExpiresAt(),
                entity.getCreatedAt()
        );
    }

    public TaxPaymentApprovalEntity toEntity(PaymentApproval approval) {
        TaxPaymentApprovalEntity entity = new TaxPaymentApprovalEntity();
        entity.setId(approval.id());
        entity.setPaymentId(approval.paymentId());
        entity.setMakerId(approval.makerId());
        entity.setCheckerId(approval.checkerId());
        entity.setDecision(approval.decision().name());
        entity.setNotes(approval.notes());
        entity.setDecidedAt(approval.decidedAt());
        entity.setCreatedAt(approval.createdAt());
        return entity;
    }

    public PaymentApproval toDomain(TaxPaymentApprovalEntity entity) {
        return PaymentApproval.rehydrate(
                entity.getId(),
                entity.getPaymentId(),
                entity.getMakerId(),
                entity.getCheckerId(),
                ApprovalDecision.valueOf(entity.getDecision()),
                entity.getNotes(),
                entity.getDecidedAt(),
                entity.getCreatedAt()
        );
    }

    public OutboxEventEntity toEntity(OutboxEvent event) {
        OutboxEventEntity entity = new OutboxEventEntity();
        entity.setId(event.id());
        entity.setAggregateType(event.aggregateType());
        entity.setAggregateId(event.aggregateId());
        entity.setEventType(event.eventType());
        entity.setEventVersion(event.eventVersion());
        entity.setTopic(event.topic());
        entity.setMessageKey(event.messageKey());
        entity.setPayload(event.payload());
        entity.setHeaders(event.headers());
        entity.setStatus(event.status());
        entity.setAttempts(event.attempts());
        entity.setNextAttemptAt(event.nextAttemptAt());
        entity.setCreatedAt(event.createdAt());
        entity.setPublishedAt(event.publishedAt());
        return entity;
    }

    public OutboxEvent toDomain(OutboxEventEntity entity) {
        return new OutboxEvent(
                entity.getId(),
                entity.getAggregateType(),
                entity.getAggregateId(),
                entity.getEventType(),
                entity.getEventVersion(),
                entity.getTopic(),
                entity.getMessageKey(),
                entity.getPayload(),
                entity.getHeaders(),
                entity.getStatus(),
                entity.getAttempts(),
                entity.getNextAttemptAt(),
                entity.getCreatedAt(),
                entity.getPublishedAt()
        );
    }
}
