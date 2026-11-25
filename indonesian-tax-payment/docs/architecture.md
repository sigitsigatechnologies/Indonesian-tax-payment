# Architecture

## Style

- Domain-Driven Design with bounded contexts per service.
- Hexagonal layering inside each service:
  - `domain` — aggregates, state machine, ports, domain exceptions.
  - `application` — use cases, transaction boundaries, orchestration.
  - `infrastructure` — JPA entities, repositories, adapters, external clients.
  - `interfaces` — REST controllers, DTOs, exception handlers.
- Event-driven integration through Kafka with a transactional outbox and consumer inbox (implemented in Phase 3).
- Saga orchestration owned by Payment Service, with deterministic references, inquiry-based uncertainty resolution and compensation.

The Payment domain aggregate `TaxPayment` has no JPA annotations. `TaxPaymentEntity` and `TaxPaymentPersistenceAdapter` implement the `TaxPaymentRepository` port, so the domain can be tested without a database.

## Service boundaries

| Service | Owns | Never does |
|---|---|---|
| API Gateway | Routing, rate limiting, correlation ID, edge errors | Business state |
| Payment Service | Payment lifecycle, idempotency, saga state | Direct access to billing/bank/government DBs |
| Billing Service | Tax billings, taxpayer profiles, validation history | Debit or government submission |
| Core Banking Mock | Accounts, bank transactions, reversals (mock) | Tax business rules |
| Tax Gateway Mock | Government billing/payment records (mock) | Bank account state |
| Reconciliation Service | Batches, items, exceptions | Mutating payment state directly |
| Notification Service | Requests, attempts, templates | Financial state |

## Data ownership

Database per service. There are no cross-database foreign keys. A reference such as `billingId` or `paymentId` is an opaque identifier owned by another service. Consistency is achieved with REST calls and, later, events plus reconciliation.

PostgreSQL is authoritative for payments, idempotency and financial transaction records. Redis is used only for acceleration, rate limiting and ephemeral coordination. Redis is never the financial system of record.

## Reliability principles

1. Never blindly retry an account debit.
2. Kafka is at-least-once: every consumer must be idempotent.
3. An HTTP timeout is an unknown result, not a failure. Resolve it with transaction inquiry.
4. Reverse a debit only when business rules and external status permit it.
5. Status changes go through `PaymentStateMachine` and create history rows.
6. Use optimistic locking (`@Version`) for financial aggregates.
7. Keep an immutable audit trail.

## Runtime topology

```
api-gateway :8080
  ├── payment-service :8082  → payment_db
  ├── billing-service :8083  → billing_db
  ├── core-banking-mock :8084 → core_banking_db
  ├── tax-gateway-mock :8085 → tax_gateway_db
  ├── reconciliation-service :8086 → reconciliation_db
  └── notification-service :8087 → notification_db

PostgreSQL :5432, Redis :6379, Kafka :9094 (host) / :9092 (docker), Kafka UI :8090,
Prometheus :9090, Grafana :3000
```

The gateway routes to static URIs configured through environment variables, issues demo HS256 JWTs at `/api/v1/auth/token`, validates tokens on every `/api/**` route and applies Redis rate limiting. Service discovery is intentionally deferred; production deployment would use Kubernetes DNS or a discovery service.

Security is enforced in two layers: gateway authentication and service-level resource-server validation. Sensitive checker operations also use method-level `@PreAuthorize`. Every service writes structured JSON logs with `correlationId`, `traceId` and `spanId`, and exports spans via OTLP when a collector is available.
