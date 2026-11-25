CREATE TABLE tax_payments (
    id                    UUID PRIMARY KEY,
    payment_reference     VARCHAR(40)    NOT NULL,
    billing_id            VARCHAR(64)    NOT NULL,
    customer_id           VARCHAR(64)    NOT NULL,
    debit_account_number  VARCHAR(34)    NOT NULL,
    amount                NUMERIC(19, 2) NOT NULL CHECK (amount > 0),
    currency              VARCHAR(3)        NOT NULL,
    status                VARCHAR(40)    NOT NULL,
    version               BIGINT         NOT NULL DEFAULT 0,
    created_at            TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at            TIMESTAMPTZ    NOT NULL DEFAULT now(),
    CONSTRAINT uk_tax_payments_reference UNIQUE (payment_reference)
);
CREATE INDEX idx_tax_payments_customer ON tax_payments (customer_id, created_at DESC);
CREATE INDEX idx_tax_payments_billing ON tax_payments (billing_id);
CREATE INDEX idx_tax_payments_status ON tax_payments (status);

CREATE TABLE tax_payment_details (
    id                 UUID PRIMARY KEY,
    payment_id         UUID           NOT NULL REFERENCES tax_payments (id),
    tax_type           VARCHAR(64)    NOT NULL,
    tax_period         VARCHAR(32),
    taxpayer_npwp      VARCHAR(32),
    principal_amount   NUMERIC(19, 2) NOT NULL CHECK (principal_amount >= 0),
    penalty_amount     NUMERIC(19, 2) NOT NULL DEFAULT 0 CHECK (penalty_amount >= 0),
    total_amount       NUMERIC(19, 2) NOT NULL CHECK (total_amount > 0),
    created_at         TIMESTAMPTZ    NOT NULL DEFAULT now(),
    CONSTRAINT uk_tax_payment_details_payment UNIQUE (payment_id)
);
CREATE INDEX idx_tax_payment_details_npwp ON tax_payment_details (taxpayer_npwp);

CREATE TABLE tax_payment_approvals (
    id           UUID PRIMARY KEY,
    payment_id   UUID         NOT NULL REFERENCES tax_payments (id),
    maker_id     VARCHAR(128) NOT NULL,
    checker_id   VARCHAR(128),
    decision     VARCHAR(32)  NOT NULL,
    notes        VARCHAR(512),
    decided_at   TIMESTAMPTZ,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uk_tax_payment_approvals_payment UNIQUE (payment_id),
    CONSTRAINT ck_tax_payment_approvals_decision CHECK (decision IN ('PENDING', 'APPROVED', 'REJECTED'))
);
CREATE INDEX idx_tax_payment_approvals_checker ON tax_payment_approvals (checker_id);

CREATE TABLE payment_status_histories (
    id             UUID PRIMARY KEY,
    payment_id     UUID         NOT NULL REFERENCES tax_payments (id),
    from_status    VARCHAR(40),
    to_status      VARCHAR(40)  NOT NULL,
    reason         VARCHAR(255),
    actor          VARCHAR(128) NOT NULL,
    correlation_id VARCHAR(128),
    occurred_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX idx_payment_status_histories_payment ON payment_status_histories (payment_id, occurred_at);

CREATE TABLE payment_idempotency (
    id               UUID PRIMARY KEY,
    idempotency_key  VARCHAR(128) NOT NULL,
    request_hash     VARCHAR(64)  NOT NULL,
    payment_id       UUID         NOT NULL REFERENCES tax_payments (id),
    expires_at       TIMESTAMPTZ  NOT NULL,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uk_payment_idempotency_key UNIQUE (idempotency_key)
);
CREATE INDEX idx_payment_idempotency_expires ON payment_idempotency (expires_at);

CREATE TABLE bank_debit_transactions (
    id                     UUID PRIMARY KEY,
    payment_id             UUID           NOT NULL REFERENCES tax_payments (id),
    debit_reference        VARCHAR(64)    NOT NULL,
    account_number         VARCHAR(34)    NOT NULL,
    amount                 NUMERIC(19, 2) NOT NULL CHECK (amount > 0),
    currency               VARCHAR(3)        NOT NULL,
    status                 VARCHAR(32)    NOT NULL,
    core_banking_reference VARCHAR(64),
    request_payload        JSONB,
    response_payload       JSONB,
    debit_at               TIMESTAMPTZ,
    created_at             TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at             TIMESTAMPTZ    NOT NULL DEFAULT now(),
    version                BIGINT         NOT NULL DEFAULT 0,
    CONSTRAINT uk_bank_debit_transactions_reference UNIQUE (debit_reference)
);
CREATE INDEX idx_bank_debit_transactions_payment ON bank_debit_transactions (payment_id);
CREATE INDEX idx_bank_debit_transactions_core_ref ON bank_debit_transactions (core_banking_reference);

CREATE TABLE government_payment_transactions (
    id                    UUID PRIMARY KEY,
    payment_id            UUID           NOT NULL REFERENCES tax_payments (id),
    submission_reference  VARCHAR(64)    NOT NULL,
    billing_id            VARCHAR(64)    NOT NULL,
    amount                NUMERIC(19, 2) NOT NULL CHECK (amount > 0),
    currency              VARCHAR(3)        NOT NULL,
    status                VARCHAR(32)    NOT NULL,
    mock_ntb              VARCHAR(32),
    mock_ntpn             VARCHAR(32),
    request_payload       JSONB,
    response_payload      JSONB,
    submitted_at          TIMESTAMPTZ,
    responded_at          TIMESTAMPTZ,
    created_at            TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at            TIMESTAMPTZ    NOT NULL DEFAULT now(),
    version               BIGINT         NOT NULL DEFAULT 0,
    CONSTRAINT uk_government_payment_transactions_reference UNIQUE (submission_reference)
);
CREATE INDEX idx_government_payment_transactions_payment ON government_payment_transactions (payment_id);
CREATE INDEX idx_government_payment_transactions_billing ON government_payment_transactions (billing_id);

CREATE TABLE reversal_transactions (
    id                    UUID PRIMARY KEY,
    payment_id            UUID           NOT NULL REFERENCES tax_payments (id),
    debit_transaction_id  UUID           REFERENCES bank_debit_transactions (id),
    reversal_reference    VARCHAR(64)    NOT NULL,
    amount                NUMERIC(19, 2) NOT NULL CHECK (amount > 0),
    currency              VARCHAR(3)        NOT NULL,
    status                VARCHAR(32)    NOT NULL,
    reason                VARCHAR(255)   NOT NULL,
    requested_at          TIMESTAMPTZ    NOT NULL DEFAULT now(),
    completed_at          TIMESTAMPTZ,
    created_at            TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at            TIMESTAMPTZ    NOT NULL DEFAULT now(),
    version               BIGINT         NOT NULL DEFAULT 0,
    CONSTRAINT uk_reversal_transactions_reference UNIQUE (reversal_reference)
);
CREATE INDEX idx_reversal_transactions_payment ON reversal_transactions (payment_id);

CREATE TABLE outbox_events (
    id              UUID PRIMARY KEY,
    aggregate_type  VARCHAR(64)  NOT NULL,
    aggregate_id    VARCHAR(64)  NOT NULL,
    event_type      VARCHAR(128) NOT NULL,
    event_version   INTEGER      NOT NULL DEFAULT 1,
    topic           VARCHAR(128) NOT NULL,
    message_key     VARCHAR(128) NOT NULL,
    payload         JSONB        NOT NULL,
    headers         JSONB,
    status          VARCHAR(32)  NOT NULL DEFAULT 'PENDING',
    attempts        INTEGER      NOT NULL DEFAULT 0,
    next_attempt_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    published_at    TIMESTAMPTZ,
    CONSTRAINT ck_outbox_events_status CHECK (status IN ('PENDING', 'PUBLISHED', 'FAILED'))
);
CREATE INDEX idx_outbox_events_pending ON outbox_events (status, next_attempt_at);
CREATE INDEX idx_outbox_events_aggregate ON outbox_events (aggregate_type, aggregate_id);

CREATE TABLE processed_events (
    id             UUID PRIMARY KEY,
    consumer_group VARCHAR(128) NOT NULL,
    event_id       UUID         NOT NULL,
    topic          VARCHAR(128) NOT NULL,
    partition_no   INTEGER,
    offset_no      BIGINT,
    processed_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uk_processed_events_consumer_event UNIQUE (consumer_group, event_id)
);
CREATE INDEX idx_processed_events_event ON processed_events (event_id);

CREATE TABLE audit_logs (
    id             UUID PRIMARY KEY,
    aggregate_type VARCHAR(64)  NOT NULL,
    aggregate_id   VARCHAR(64)  NOT NULL,
    action         VARCHAR(64)  NOT NULL,
    actor          VARCHAR(128) NOT NULL,
    correlation_id VARCHAR(128),
    before_state   JSONB,
    after_state    JSONB,
    occurred_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX idx_audit_logs_aggregate ON audit_logs (aggregate_type, aggregate_id, occurred_at DESC);
CREATE INDEX idx_audit_logs_correlation ON audit_logs (correlation_id);
