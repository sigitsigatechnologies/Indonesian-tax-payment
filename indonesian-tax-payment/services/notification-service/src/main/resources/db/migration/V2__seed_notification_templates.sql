INSERT INTO notification_templates (id, template_code, channel, subject, body, active, created_at, updated_at)
VALUES
    ('55555555-5555-5555-5555-555555555551', 'TAX_PAYMENT_SUCCESS', 'WEBHOOK',
     'Tax payment completed',
     'Payment {{paymentReference}} for billing {{billingId}} succeeded with reference {{governmentReference}}.',
     TRUE, now(), now()),
    ('55555555-5555-5555-5555-555555555552', 'TAX_PAYMENT_FAILED', 'WEBHOOK',
     'Tax payment failed',
     'Payment {{paymentReference}} for billing {{billingId}} failed: {{reason}}.',
     TRUE, now(), now()),
    ('55555555-5555-5555-5555-555555555553', 'TAX_PAYMENT_REVERSED', 'WEBHOOK',
     'Tax payment reversed',
     'Payment {{paymentReference}} was reversed. Reversal reference {{reversalReference}}.',
     TRUE, now(), now());
