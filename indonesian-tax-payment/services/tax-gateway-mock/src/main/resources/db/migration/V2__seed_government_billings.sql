INSERT INTO government_billing_records (id, billing_id, taxpayer_id, amount, currency, status, created_at, updated_at)
VALUES
    ('44444444-4444-4444-4444-444444444441', 'BILL-2026-0001', 'TAXPAYER-001', 1500000.00, 'IDR', 'OPEN', now(), now()),
    ('44444444-4444-4444-4444-444444444442', 'BILL-2026-0002', 'TAXPAYER-001', 750000.00, 'IDR', 'OPEN', now(), now()),
    ('44444444-4444-4444-4444-444444444443', 'BILL-2026-0003', 'TAXPAYER-001', 5000000.00, 'IDR', 'PAID', now(), now());
