CREATE TABLE taxpayer_profiles (
    id             UUID PRIMARY KEY,
    taxpayer_id    VARCHAR(64)  NOT NULL,
    taxpayer_name  VARCHAR(255) NOT NULL,
    npwp           VARCHAR(32)  NOT NULL,
    status         VARCHAR(32)  NOT NULL,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uk_taxpayer_profiles_taxpayer UNIQUE (taxpayer_id),
    CONSTRAINT uk_taxpayer_profiles_npwp UNIQUE (npwp)
);

CREATE TABLE tax_billings (
    id                UUID PRIMARY KEY,
    billing_id        VARCHAR(64)    NOT NULL,
    taxpayer_id       VARCHAR(64)    NOT NULL,
    tax_type          VARCHAR(64)    NOT NULL,
    tax_period        VARCHAR(32),
    principal_amount  NUMERIC(19, 2) NOT NULL CHECK (principal_amount >= 0),
    penalty_amount    NUMERIC(19, 2) NOT NULL DEFAULT 0 CHECK (penalty_amount >= 0),
    total_amount      NUMERIC(19, 2) NOT NULL CHECK (total_amount > 0),
    currency          VARCHAR(3)        NOT NULL,
    status            VARCHAR(32)    NOT NULL,
    due_date          DATE           NOT NULL,
    paid_at           TIMESTAMPTZ,
    created_at        TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ    NOT NULL DEFAULT now(),
    version           BIGINT         NOT NULL DEFAULT 0,
    CONSTRAINT uk_tax_billings_billing_id UNIQUE (billing_id),
    CONSTRAINT fk_tax_billings_taxpayer FOREIGN KEY (taxpayer_id)
        REFERENCES taxpayer_profiles (taxpayer_id),
    CONSTRAINT ck_tax_billings_status CHECK (status IN ('ACTIVE', 'PAID', 'EXPIRED', 'CANCELLED')),
    CONSTRAINT ck_tax_billings_total CHECK (total_amount = principal_amount + penalty_amount)
);
CREATE INDEX idx_tax_billings_taxpayer ON tax_billings (taxpayer_id);
CREATE INDEX idx_tax_billings_status_due ON tax_billings (status, due_date);

CREATE TABLE billing_validation_histories (
    id                 UUID PRIMARY KEY,
    billing_id         VARCHAR(64) NOT NULL,
    validation_result  VARCHAR(32) NOT NULL,
    reason             VARCHAR(255),
    correlation_id     VARCHAR(128),
    validated_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_billing_validation_billing FOREIGN KEY (billing_id)
        REFERENCES tax_billings (billing_id)
);
CREATE INDEX idx_billing_validation_billing ON billing_validation_histories (billing_id, validated_at DESC);
