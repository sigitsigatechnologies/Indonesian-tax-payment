CREATE TABLE government_billing_records (
    id           UUID PRIMARY KEY,
    billing_id   VARCHAR(64)    NOT NULL,
    taxpayer_id  VARCHAR(64)    NOT NULL,
    amount       NUMERIC(19, 2) NOT NULL CHECK (amount > 0),
    currency     VARCHAR(3)        NOT NULL,
    status       VARCHAR(32)    NOT NULL,
    created_at   TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ    NOT NULL DEFAULT now(),
    CONSTRAINT uk_government_billing_records_billing UNIQUE (billing_id)
);

CREATE TABLE government_tax_payments (
    id            UUID PRIMARY KEY,
    reference     VARCHAR(64)    NOT NULL,
    billing_id    VARCHAR(64)    NOT NULL,
    amount        NUMERIC(19, 2) NOT NULL CHECK (amount > 0),
    currency      VARCHAR(3)        NOT NULL,
    status        VARCHAR(32)    NOT NULL,
    mock_ntb      VARCHAR(32),
    mock_ntpn     VARCHAR(32),
    submitted_at  TIMESTAMPTZ,
    responded_at  TIMESTAMPTZ,
    created_at    TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ    NOT NULL DEFAULT now(),
    version       BIGINT         NOT NULL DEFAULT 0,
    CONSTRAINT uk_government_tax_payments_reference UNIQUE (reference),
    CONSTRAINT fk_government_tax_payments_billing FOREIGN KEY (billing_id)
        REFERENCES government_billing_records (billing_id)
);
CREATE INDEX idx_government_tax_payments_billing ON government_tax_payments (billing_id);
CREATE INDEX idx_government_tax_payments_status ON government_tax_payments (status);

CREATE TABLE government_payment_responses (
    id                 UUID PRIMARY KEY,
    payment_reference  VARCHAR(64) NOT NULL,
    response_code      VARCHAR(32) NOT NULL,
    response_message   VARCHAR(512),
    received_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_government_payment_responses_payment FOREIGN KEY (payment_reference)
        REFERENCES government_tax_payments (reference)
);
CREATE INDEX idx_government_payment_responses_payment ON government_payment_responses (payment_reference);
