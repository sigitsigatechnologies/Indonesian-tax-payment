INSERT INTO bank_accounts (id, account_number, account_name, customer_id, currency,
                           available_balance, ledger_balance, status, version, created_at, updated_at)
VALUES
    ('33333333-3333-3333-3333-333333333331', '1234567890', 'PT Nusantara Digital Sejahtera',
     'CUST-001', 'IDR', 250000000.00, 250000000.00, 'ACTIVE', 0, now(), now()),
    ('33333333-3333-3333-3333-333333333332', '9876543210', 'PT Nusantara Digital Sejahtera - Low Balance',
     'CUST-001', 'IDR', 100000.00, 100000.00, 'ACTIVE', 0, now(), now()),
    ('33333333-3333-3333-3333-333333333333', '5555555555', 'PT Nusantara Digital Sejahtera - Blocked',
     'CUST-001', 'IDR', 5000000.00, 5000000.00, 'BLOCKED', 0, now(), now());
