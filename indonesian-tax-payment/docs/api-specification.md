# API Specification

All endpoints are JSON. The gateway propagates and returns `X-Correlation-ID`. Idempotent creation requires `Idempotency-Key`. All `/api/**` calls require `Authorization: Bearer <jwt>`.

| Method | Path | Description |
|---|---|---|
| POST | /api/v1/auth/token | Demo token issuer (`{"username":"...","role":"MAKER|CHECKER"}`) |

## Payment Service — :8082

| Method | Path | Description |
|---|---|---|
| POST | /api/v1/tax/payments | Initiate a payment (header `Idempotency-Key`, optional `X-Actor`) |
| GET | /api/v1/tax/payments/{paymentId} | Payment by UUID |
| GET | /api/v1/tax/payments/reference/{reference} | Payment by business reference |
| GET | /api/v1/tax/payments/{paymentId}/history | Immutable status history |
| GET | /api/v1/tax/payments?customerId=&page=&size= | Customer payments, paginated |
| POST | /api/v1/tax/payments/{paymentId}/approve | Checker approves a `PENDING_APPROVAL` payment |
| POST | /api/v1/tax/payments/{paymentId}/reject | Checker rejects a `PENDING_APPROVAL` payment |
| POST | /api/v1/tax/payments/{paymentId}/reverse | Checker reverses a payment with a confirmed external outcome |
| POST | /api/v1/reconciliation/run?businessDate= | Run a reconciliation batch |
| GET | /api/v1/reconciliation/batches/{batchId} | Get a batch report |
| GET | /api/v1/reconciliation/exceptions?status=&page=&size= | List exceptions |
| POST | /api/v1/reconciliation/exceptions/{id}/resolve | Manually resolve an exception |

Create request:

```json
{
  "billingId": "BILL-2026-0001",
  "debitAccountNumber": "1234567890",
  "amount": 1500000.00,
  "currency": "IDR",
  "customerId": "CUST-001"
}
```

Validation: billing/customer/account required; amount positive with at most 2 decimals; currency 3 letters.

Response `201`:

```json
{
  "paymentId": "96523e88-9d4c-48a1-9fb7-0ae874be96be",
  "reference": "TP20261125BD09E40FA9",
  "billingId": "BILL-2026-0001",
  "customerId": "CUST-001",
  "debitAccountNumber": "****7890",
  "amount": 1500000.00,
  "currency": "IDR",
  "status": "RECEIVED",
  "version": 0,
  "createdAt": "2026-11-25T02:48:43.487Z",
  "updatedAt": "2026-11-25T02:48:43.487Z"
}
```

Account numbers are masked in responses.

Approval request (optional body):

```json
{ "notes": "billing verified" }
```

Required headers: `X-Actor` (checker identity) and optionally `X-Role: CHECKER`. When `X-Role` is supplied it must be `CHECKER`. The checker must differ from the maker stored at initiation.

- Approve: `PENDING_APPROVAL → APPROVED`.
- Reject: `PENDING_APPROVAL → FAILED`.
- Self-approval or a non-checker role: `403 FORBIDDEN`.
- Payment not awaiting approval: `409 INVALID_STATE_TRANSITION`.
- Concurrent modification: `409 CONCURRENT_MODIFICATION`.

## Billing Service — :8083

| Method | Path | Description |
|---|---|---|
| GET | /api/v1/tax/billings/{billingId} | Billing inquiry |
| POST | /api/v1/tax/billings/{billingId}/validate | Validate for payment |

Validation request (all fields optional):

```json
{ "amount": 1500000.00, "currency": "IDR", "taxpayerId": "TAXPAYER-001" }
```

Validation response:

```json
{
  "valid": true,
  "resultCode": "VALID",
  "message": "Billing is valid for payment",
  "billing": { "...": "billing snapshot" }
}
```

Business failure codes: `BILLING_INACTIVE`, `BILLING_EXPIRED`, `BILLING_ALREADY_PAID`, `TAXPAYER_MISMATCH`, `AMOUNT_MISMATCH`, `CURRENCY_MISMATCH`.

## Core Banking Mock — :8084

| Method | Path |
|---|---|
| GET | /api/v1/accounts/{accountNumber} |
| GET | /api/v1/accounts/{accountNumber}/balance |
| GET | /api/v1/transactions/{reference} |

Additional mock endpoints:

| Method | Path | Description |
|---|---|---|
| POST | /api/v1/transactions/debit | Idempotent debit by `debitReference` |
| POST | /api/v1/transactions/{reference}/reverse | Idempotent reversal by `reversalReference` |
| GET | /api/v1/transactions/{reference}/reversal | Inquire a reversal |

Simulation is selected with header `X-Mock-Scenario`: `NONE`, `INSUFFICIENT_BALANCE`, `TIMEOUT_BEFORE_DEBIT`, `TIMEOUT_AFTER_DEBIT`, `TIMEOUT_BEFORE_REVERSAL`, `TIMEOUT_AFTER_REVERSAL`. Balance and account fixtures also support insufficient balance and blocked account cases.

## Tax Gateway Mock — :8085

| Method | Path |
|---|---|
| GET | /api/v1/government/billings/{billingId} |
| GET | /api/v1/government/payments/{reference} |

| POST | /api/v1/government/payments | Idempotent submission by `submissionReference` |

Mock scenarios via `X-Mock-Scenario`: `NONE`, `REJECTED`, `DELAYED`, `TIMEOUT_BEFORE_SUBMIT`, `TIMEOUT_AFTER_SUBMIT`. NTB/NTPN values are prefixed `MOCK-` and every response carries a disclaimer.

## Error contract

```json
{
  "timestamp": "2026-11-25T02:50:37.595Z",
  "status": 400,
  "code": "VALIDATION_ERROR",
  "message": "Request validation failed",
  "path": "/api/v1/tax/payments",
  "correlationId": "743e6102-...",
  "details": [{ "field": "billingId", "message": "must not be blank" }]
}
```

Gateway maps downstream connection failures to `503 UPSTREAM_UNAVAILABLE`, timeouts to `504 UPSTREAM_TIMEOUT`, and unknown routes to `404 NOT_FOUND`.

## OpenAPI

Swagger UI: `/swagger-ui.html`. OpenAPI JSON: `/v3/api-docs`.
