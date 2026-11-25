# Database ERD

All tables use UUID primary keys, UTC `TIMESTAMPTZ` timestamps and `NUMERIC(19,2)` for money. Each database is owned by exactly one service.

## payment_db

```
tax_payments 1 ── * payment_status_histories
tax_payments 1 ── 1 payment_idempotency
tax_payments 1 ── 1 tax_payment_details
tax_payments 1 ── 1 tax_payment_approvals
tax_payments 1 ── * bank_debit_transactions
tax_payments 1 ── * government_payment_transactions
tax_payments 1 ── * reversal_transactions
bank_debit_transactions 1 ── * reversal_transactions

outbox_events        (no FK; event relay)
processed_events     (no FK; consumer inbox)
audit_logs           (no FK; immutable audit)
```

Key constraints:

- `tax_payments.payment_reference` unique, `amount > 0`.
- `bank_debit_transactions.debit_reference` unique.
- `government_payment_transactions.submission_reference` unique.
- `reversal_transactions.reversal_reference` unique.
- `payment_idempotency.idempotency_key` unique.
- `processed_events(consumer_group, event_id)` unique.
- `@Version` on payment, debit, government and reversal aggregates.

## billing_db

```
taxpayer_profiles 1 ── * tax_billings
tax_billings 1 ── * billing_validation_histories   (FK on billing_id)
```

`billing_id` and `npwp` are unique. `total_amount = principal_amount + penalty_amount` is enforced by a check constraint.

## core_banking_db

```
bank_accounts 1 ── * bank_transactions
bank_accounts 1 ── * bank_reversal_transactions
```

Account number unique; available and ledger balances non-negative; transaction and reversal references unique.

## tax_gateway_db

```
government_billing_records 1 ── * government_tax_payments
government_tax_payments 1 ── * government_payment_responses
```

`mock_ntb` and `mock_ntpn` are explicitly named mock fields. They are never official DJP/MPN references.

## reconciliation_db

```
reconciliation_batches 1 ── * reconciliation_items
reconciliation_items 1 ── * reconciliation_exceptions
```

Item match status: `MATCHED`, `MISSING_DEBIT`, `MISSING_GOVERNMENT`, `DUPLICATE`, `AMOUNT_MISMATCH`.

## notification_db

```
notification_requests 1 ── * notification_attempts
notification_templates (lookup by template_code)
```

`notification_id` and `(notification_id, attempt_number)` are unique.

## Additional Phase 3–6 objects

- `payment_db.processed_events`, `notification_db.processed_events`, `reconciliation_db.processed_events`: consumer inbox with unique `(consumer_group, event_id)`.
- `reconciliation_db.outbox_events`: outbox for `TAX_PAYMENT_RECONCILIATION_COMPLETED`.
- `reconciliation_items.government_amount`: stores the government-side amount so the matching engine can detect `AMOUNT_MISMATCH`.

No new databases were introduced; all additions live inside the owning service's database.
