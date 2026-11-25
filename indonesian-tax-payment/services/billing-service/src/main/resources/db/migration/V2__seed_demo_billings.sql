INSERT INTO taxpayer_profiles (id, taxpayer_id, taxpayer_name, npwp, status, created_at, updated_at)
VALUES ('11111111-1111-1111-1111-111111111111', 'TAXPAYER-001', 'PT Nusantara Digital Sejahtera',
        '01.234.567.8-901.000', 'ACTIVE', now(), now());

INSERT INTO tax_billings (id, billing_id, taxpayer_id, tax_type, tax_period, principal_amount,
                          penalty_amount, total_amount, currency, status, due_date, paid_at,
                          created_at, updated_at, version)
VALUES
    ('22222222-2222-2222-2222-222222222221', 'BILL-2026-0001', 'TAXPAYER-001', 'PPN_DALAM_NEGERI',
     '2026-10', 1500000.00, 0.00, 1500000.00, 'IDR', 'ACTIVE', DATE '2026-12-31', NULL, now(), now(), 0),
    ('22222222-2222-2222-2222-222222222222', 'BILL-2026-0002', 'TAXPAYER-001', 'PPh_PASAL_21',
     '2025-12', 750000.00, 0.00, 750000.00, 'IDR', 'ACTIVE', DATE '2025-01-31', NULL, now(), now(), 0),
    ('22222222-2222-2222-2222-222222222223', 'BILL-2026-0003', 'TAXPAYER-001', 'PPh_BADAN',
     '2025-12', 5000000.00, 0.00, 5000000.00, 'IDR', 'PAID', DATE '2026-04-30', now(), now(), now(), 0);
