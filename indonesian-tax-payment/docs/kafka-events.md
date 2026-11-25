# Kafka Events

## Envelope

Every message uses `id.co.taxpay.common.event.EventEnvelope<T>`:

```json
{
  "eventId": "9d1c2f5e-...",
  "eventType": "TAX_PAYMENT_COMPLETED",
  "eventVersion": 1,
  "aggregateId": "payment-uuid",
  "correlationId": "request-correlation-id",
  "causationId": "parent-event-id",
  "timestamp": "2026-11-25T02:48:43.487Z",
  "source": "payment-service",
  "payload": {}
}
```

Message key: `paymentId`. Partition count: 3. Producer: `acks=all`, idempotence enabled. Consumer groups are service-scoped (`payment-service`, `reconciliation-service`, `notification-service`).

## Topics

| Topic | Producer | Intended consumers |
|---|---|---|
| tax.payment.initiated | Payment | Audit/notification |
| tax.payment.validated | Payment | Notification |
| tax.payment.approved | Payment | Payment saga |
| tax.payment.debit.requested | Payment | Payment debit worker |
| tax.payment.debit.completed | Payment | Saga, reconciliation |
| tax.payment.debit.failed | Payment | Saga, reconciliation |
| tax.payment.government.submitted | Payment | Saga, reconciliation |
| tax.payment.completed | Payment | Notification, reconciliation |
| tax.payment.failed | Payment | Notification, reconciliation |
| tax.payment.reversal.requested | Payment | Reversal worker |
| tax.payment.reversed | Payment | Notification, reconciliation |
| tax.payment.reconciliation.completed | Reconciliation | Notification |
| tax.payment.notification.requested | Payment | Notification |
| tax.payment.dlt | All | Operations |

## Outbox and inbox (implemented in Phase 3)

- `outbox_events` stores the complete `EventEnvelope` as JSONB in the same PostgreSQL transaction as the payment state change, history row and audit row.
- `OutboxRelay` polls pending rows with `FOR UPDATE SKIP LOCKED` so multiple instances can run safely.
- The relay publishes with `paymentId` as the Kafka key and waits for the broker acknowledgement before marking the row `PUBLISHED`.
- Failed publishes increment `attempts`, set `next_attempt_at` with exponential backoff (1s, 2s, 4s, 8s, capped at 5 minutes) and stay `PENDING`.
- After 5 attempts the row becomes `FAILED` and the payload is published to `tax.payment.dlt`.
- `processed_events(consumer_group, event_id)` provides consumer idempotency. Notification Service persists this row in the same transaction as the notification request, so a duplicate delivery is ignored.
- Notification Service uses `DefaultErrorHandler` with a `DeadLetterPublishingRecoverer` and exponential backoff. Malformed JSON is non-retryable and goes directly to `tax.payment.dlt`.

Topics are created by the `kafka-init` Compose service with auto-topic creation disabled.
