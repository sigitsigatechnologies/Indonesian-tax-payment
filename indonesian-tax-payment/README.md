# Indonesian Tax Payment Microservices

Enterprise-style simulation of an Indonesian corporate tax payment platform: corporate internet banking / CMS to bank core banking to a DJP/MPN-style tax gateway, with reconciliation and notification.

> **Mock disclaimer:** every bank and government integration in this repository is a mock. NTB/NTPN values, account balances and government responses are simulated test data. No official DJP/MPN API contract is reproduced. The adapters are designed so real contracts can be implemented later without changing the payment domain.

## Status — All 8 phases complete

| Area | Delivered |
|---|---|
| Build | Maven multi-module, Java 21 release, Spring Boot 3.5.9, Spring Cloud 2025.0.2 |
| Common library | Event envelope, topic/event constants, error codes, API error model, correlation IDs, JWT encoder/decoder, JSON mapper |
| Payment service | Full lifecycle, concurrency-safe idempotency, state machine, optimistic locking, audit history, saga orchestrator |
| Integration clients | OpenFeign clients to Billing, Core Banking and Tax Gateway with Resilience4j circuit breakers and retries |
| Core banking mock | Account/balance inquiry, idempotent debit, reversal, insufficient-balance/timeout/duplicate simulations |
| Tax gateway mock | Billing inquiry, idempotent submission, simulated NTB/NTPN, rejected/timeout/delayed simulations |
| Saga | Validation → maker-checker → debit → government submission → confirmation, inquiry-based uncertainty resolution and compensation |
| Reconciliation | Kafka event projection, batch matching engine, exception detection, manual resolution and reports |
| Notification | Webhook notification requests from payment events, inbox idempotency, retry-ready attempt schema |
| Events | Transactional outbox in payment and reconciliation, inbox idempotency, exponential backoff, DLT |
| Security | HS256 JWT, `/api/v1/auth/token` demo issuer, RBAC, maker-checker segregation, sensitive data masking |
| Observability | Structured JSON logs with correlation/trace IDs, Micrometer Prometheus, OpenTelemetry OTLP tracing |
| Infra | PostgreSQL 17, Redis 7, Kafka 3.9 KRaft, Kafka UI, Prometheus, Grafana in Docker Compose |
| Tests | 15 tests: 12 unit + 3 Testcontainers/REST Assured integration tests (skipped when Docker is unavailable) |

This is a demonstration platform using mock bank/government adapters. Real DJP/MPN contracts, a production IdP and Kubernetes deployment are out of scope.

## Services and ports

| Service | Port | Responsibility |
|---|---|---|
| API Gateway | 8080 | Entry point, JWT validation, routing, rate limiting, correlation ID, demo token issuer |
| Payment Service | 8082 | Payment lifecycle, saga orchestration, outbox, maker-checker |
| Billing Service | 8083 | Billing inquiry and validation |
| Core Banking Mock | 8084 | Account/balance inquiry, debit, reversal, failure simulations |
| Tax Gateway Mock | 8085 | Government billing/payment inquiry, submission, mock references |
| Reconciliation Service | 8086 | Reconciliation batches, matching, exceptions, manual resolution |
| Notification Service | 8087 | Notification requests from events, delivery attempts |

## Architecture

```
Corporate CMS
      |
      v
 API Gateway :8080  (routing, rate limit, correlation id)
      |
       +--> Payment Service :8082  --Feign--> Billing Service :8083
       |         |                            Core Banking Mock :8084
       |         |                            Tax Gateway Mock :8085
       |         +---- Kafka outbox ----> Reconciliation :8086 / Notification :8087
      |
      +--> PostgreSQL (database per service) + Redis + Kafka
```

Each service follows `domain / application / infrastructure / interfaces` layering. Payment domain logic is framework-independent; JPA entities and adapters live in `infrastructure`.

### Database per service

`payment_db`, `billing_db`, `core_banking_db`, `tax_gateway_db`, `reconciliation_db`, `notification_db`.

No service reads another service's database. Cross-service communication is REST today and Kafka from Phase 3.

## Quick start

Prerequisites: JDK 21+, Maven 3.9+, Docker with Compose.

```bash
cd indonesian-tax-payment
cp .env.example .env

docker compose up -d postgres redis kafka kafka-init kafka-ui prometheus grafana

mvn clean package

# run each service in its own terminal (or use an IDE)
mvn -pl services/payment-service spring-boot:run
mvn -pl services/billing-service spring-boot:run
mvn -pl services/core-banking-mock spring-boot:run
mvn -pl services/tax-gateway-mock spring-boot:run
mvn -pl services/reconciliation-service spring-boot:run
mvn -pl services/notification-service spring-boot:run
mvn -pl services/api-gateway spring-boot:run
```

Infra endpoints:

- Kafka UI: http://localhost:8090
- Prometheus: http://localhost:9090
- Grafana: http://localhost:3000 (admin/admin by default)
- Swagger UI: `http://localhost:8082/swagger-ui.html`, `:8083`, `:8084`, `:8085`
- Health: `http://localhost:8082/actuator/health`

Default local credentials are development-only and come from `.env` / environment variables. Never commit real secrets.

## API examples

All `/api/**` endpoints require a Bearer JWT. Obtain a demo token from the gateway (HS256 secret from `JWT_SECRET`):

```bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/token \
  -H 'Content-Type: application/json' \
  -d '{"username":"maker.one","role":"MAKER"}' | jq -r .accessToken)
```

Use `role: CHECKER` for approval/rejection/reversal. Pass `-H "Authorization: Bearer $TOKEN"` on every request below.

Initiate a payment (idempotency is mandatory and persisted in PostgreSQL):

```bash
curl -X POST http://localhost:8080/api/v1/tax/payments \
  -H 'Content-Type: application/json' \
  -H 'Idempotency-Key: demo-key-001' \
  -H 'X-Actor: maker.demo' \
  -H 'X-Correlation-ID: demo-correlation-1' \
  -d '{
    "billingId": "BILL-2026-0001",
    "debitAccountNumber": "1234567890",
    "amount": 1500000.00,
    "currency": "IDR",
    "customerId": "CUST-001"
  }'
```

Inquiry and history:

```bash
curl http://localhost:8080/api/v1/tax/payments/{paymentId}
curl http://localhost:8080/api/v1/tax/payments/reference/{reference}
curl http://localhost:8080/api/v1/tax/payments/{paymentId}/history
curl 'http://localhost:8080/api/v1/tax/payments?customerId=CUST-001&page=0&size=20'
```

Maker-checker approval (payment must be `PENDING_APPROVAL`; the checker must differ from the maker):

```bash
curl -X POST http://localhost:8080/api/v1/tax/payments/{paymentId}/approve \
  -H 'Content-Type: application/json' \
  -H 'X-Actor: checker.demo' \
  -H 'X-Role: CHECKER' \
  -d '{"notes":"billing verified"}'

curl -X POST http://localhost:8080/api/v1/tax/payments/{paymentId}/reject \
  -H 'Content-Type: application/json' \
  -H 'X-Actor: checker.demo' \
  -H 'X-Role: CHECKER' \
  -d '{"notes":"invalid tax period"}'
```

Reversal and reconciliation:

```bash
curl -X POST http://localhost:8080/api/v1/tax/payments/{paymentId}/reverse \
  -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"reason":"MANUAL_CORRECTION"}'

curl -X POST 'http://localhost:8080/api/v1/reconciliation/run?businessDate=2026-11-25' \
  -H "Authorization: Bearer $TOKEN" -H 'X-Actor: ops'
curl 'http://localhost:8080/api/v1/reconciliation/exceptions?page=0&size=20' \
  -H "Authorization: Bearer $TOKEN"
```

Billing validation:

```bash
curl -X POST http://localhost:8080/api/v1/tax/billings/BILL-2026-0001/validate \
  -H 'Content-Type: application/json' \
  -d '{"amount":1500000.00,"currency":"IDR","taxpayerId":"TAXPAYER-001"}'
```

Mock inquiries:

```bash
curl http://localhost:8080/api/v1/accounts/1234567890
curl http://localhost:8080/api/v1/accounts/1234567890/balance
curl http://localhost:8080/api/v1/government/billings/BILL-2026-0001
```

Error responses use one contract:

```json
{
  "timestamp": "2026-11-25T02:50:37.595Z",
  "status": 409,
  "code": "IDEMPOTENCY_CONFLICT",
  "message": "The Idempotency-Key was already used for a different payment payload",
  "path": "/api/v1/tax/payments",
  "correlationId": "dff3738b-c916-4e2a-aa81-6babd2ecf6a6",
  "details": []
}
```

## Payment state machine

`RECEIVED → VALIDATING → VALIDATED → PENDING_APPROVAL → APPROVED → DEBIT_PENDING → DEBIT_SUCCESS → GOVERNMENT_SUBMISSION_PENDING → GOVERNMENT_PROCESSING → SUCCESS`

Compensation and uncertainty paths: `FAILED`, `REVERSAL_PENDING`, `REVERSED`, `UNKNOWN`, `EXPIRED`. Transitions are centralized in `PaymentStateMachine`. Initiation creates `RECEIVED`; approve/reject are available once the saga moves a payment to `PENDING_APPROVAL`. Every transition appends immutable history and an audit-log row.

## Idempotency and concurrency

- The `Idempotency-Key` request hash is SHA-256 over billing, customer, account, amount and currency.
- Replay with the same key and payload returns the original payment.
- Reuse with a different payload returns `409 IDEMPOTENCY_CONFLICT`.
- Creation runs in a transaction; the PostgreSQL unique constraint arbitrates concurrent requests. The losing transaction rolls back and returns the winner's payment.
- `@Version` optimistic locking returns `409 CONCURRENT_MODIFICATION` when two actors modify the same payment simultaneously.
- Expired idempotency records (24-hour TTL) are removed and recreated.

## Kafka topics

`tax.payment.initiated`, `tax.payment.validated`, `tax.payment.approved`, `tax.payment.debit.requested`, `tax.payment.debit.completed`, `tax.payment.debit.failed`, `tax.payment.government.submitted`, `tax.payment.completed`, `tax.payment.failed`, `tax.payment.reversal.requested`, `tax.payment.reversed`, `tax.payment.reconciliation.completed`, `tax.payment.notification.requested`, `tax.payment.dlt`.

All events use `EventEnvelope` (`eventId`, `eventType`, `eventVersion`, `aggregateId`, `correlationId`, `causationId`, `timestamp`, `source`, `payload`) and `paymentId` as the message key. Topics are provisioned by `kafka-init`.

## Event reliability

- Payment state changes and outbox rows are committed in one PostgreSQL transaction.
- The relay publishes events with `paymentId` as the Kafka key and marks rows `PUBLISHED` only after broker acknowledgement.
- Failed publishes retry with exponential backoff (1s → 2s → 4s → 8s, capped at 5 minutes). After 5 attempts the row is `FAILED` and a copy is sent to `tax.payment.dlt`.
- Consumers use a `processed_events` inbox keyed by `(consumer_group, event_id)` so duplicate deliveries are ignored.
- Malformed events and exhausted consumer retries go to `tax.payment.dlt`.

## Build and test

```bash
mvn -B clean package     # compiles all modules and runs tests
mvn -B test              # unit tests only
```

Final verification performed in this workspace:

- `mvn -B clean test` → 15 tests, 0 failures, 0 errors (3 Testcontainers tests skipped because Docker daemon was unavailable).
- `mvn -B clean package` → BUILD SUCCESS (all 8 modules).
- All seven services started against fresh PostgreSQL databases; Flyway applied all migrations and Hibernate `ddl-auto=validate` passed.
- Payment create/replay/conflict/validation/history/list, billing validation, mock inquiries and gateway routing with correlation ID and Redis rate limiting were exercised with curl.
- **Concurrent idempotency:** 10 parallel POSTs with the same key produced one payment row and one idempotency row; all clients received the same `paymentId`.
- **Maker-checker:** wrong role and self-approval returned `403`; a different checker approved to `APPROVED`; rejection moved a payment to `FAILED`; approval records and audit JSONB were verified in PostgreSQL.
- **Optimistic locking:** two parallel approvals produced one `200` and one `409 CONCURRENT_MODIFICATION`.
- **Events:** a created payment produced a `PENDING` outbox row that the relay published and marked `PUBLISHED`; rejection produced `TAX_PAYMENT_FAILED`, which the notification consumer turned into one `notification_requests` row and one `processed_events` row. Replaying the same envelope created no additional rows. Malformed JSON was routed to `tax.payment.dlt`.
- **Outbox retry:** with the broker unavailable, events retried with backoff, reached `attempts = 5`, and were marked `FAILED` after dead-letter handling.
- **Full saga:** a payment progressed `RECEIVED → VALIDATING → VALIDATED → PENDING_APPROVAL → APPROVED → DEBIT_PENDING → DEBIT_SUCCESS → GOVERNMENT_SUBMISSION_PENDING → GOVERNMENT_PROCESSING → SUCCESS`; bank debit, mock NTB/NTPN, notification request and all five published outbox events were verified in PostgreSQL.
- **Reversal:** manual reversal moved a successful payment to `REVERSED`, created a reversal transaction and restored the mock bank balance.
- **Compensation:** with `MOCK_SCENARIO=REJECTED`, government rejection drove the payment to `FAILED` and then `REVERSED` with a completed reversal.
- **Reconciliation:** ingested events produced items; a batch run generated one `MISSING_DEBIT` exception, and manual resolution marked it `RESOLVED`.
- **Security/JWT:** unauthenticated requests returned `401`; maker token was rejected on checker endpoints with `403`; checker token succeeded; structured JSON logs included correlation and trace IDs.
- Docker Compose was not started in this workspace because the Docker daemon was unavailable; local PostgreSQL, Redis and Kafka were used for runtime verification.

## Repository layout

```
indonesian-tax-payment/
├── pom.xml
├── docker-compose.yml
├── .env.example
├── common/
├── services/
│   ├── api-gateway/
│   ├── payment-service/
│   ├── billing-service/
│   ├── core-banking-mock/
│   ├── tax-gateway-mock/
│   ├── reconciliation-service/
│   └── notification-service/
├── infra/
│   ├── postgres/init/
│   ├── prometheus/
│   └── grafana/provisioning/
└── docs/
```

## Future enhancements (out of scope)

- Real DJP/MPN and core banking contracts behind the existing adapters.
- Production IdP/OIDC and fine-grained RBAC policies.
- Kubernetes deployment, service discovery and horizontal saga scaling with leader election.
- Grafana dashboards for saga duration, outbox lag and reconciliation exception rates.
