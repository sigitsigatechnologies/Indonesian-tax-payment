CREATE TABLE reconciliation_batches (
    id                 UUID PRIMARY KEY,
    batch_reference    VARCHAR(64) NOT NULL,
    business_date      DATE        NOT NULL,
    status             VARCHAR(32) NOT NULL,
    total_records      INTEGER     NOT NULL DEFAULT 0,
    matched_records    INTEGER     NOT NULL DEFAULT 0,
    exception_records  INTEGER     NOT NULL DEFAULT 0,
    started_at         TIMESTAMPTZ NOT NULL DEFAULT now(),
    completed_at       TIMESTAMPTZ,
    created_at         TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at         TIMESTAMPTZ NOT NULL DEFAULT now(),
    version            BIGINT      NOT NULL DEFAULT 0,
    CONSTRAINT uk_reconciliation_batches_reference UNIQUE (batch_reference),
    CONSTRAINT ck_reconciliation_batches_status CHECK (status IN ('RUNNING', 'COMPLETED', 'FAILED'))
);
CREATE INDEX idx_reconciliation_batches_date ON reconciliation_batches (business_date DESC);

CREATE TABLE reconciliation_items (
    id                  UUID PRIMARY KEY,
    batch_id            UUID           NOT NULL REFERENCES reconciliation_batches (id),
    payment_id          UUID,
    payment_reference   VARCHAR(64),
    debit_reference     VARCHAR(64),
    government_reference VARCHAR(64),
    amount              NUMERIC(19, 2),
    currency            VARCHAR(3),
    match_status        VARCHAR(32)    NOT NULL,
    discrepancy_amount  NUMERIC(19, 2) NOT NULL DEFAULT 0,
    created_at          TIMESTAMPTZ    NOT NULL DEFAULT now(),
    CONSTRAINT ck_reconciliation_items_match_status
        CHECK (match_status IN ('MATCHED', 'MISSING_DEBIT', 'MISSING_GOVERNMENT', 'DUPLICATE', 'AMOUNT_MISMATCH'))
);
CREATE INDEX idx_reconciliation_items_batch ON reconciliation_items (batch_id, match_status);
CREATE INDEX idx_reconciliation_items_payment ON reconciliation_items (payment_id);
CREATE INDEX idx_reconciliation_items_debit ON reconciliation_items (debit_reference);
CREATE INDEX idx_reconciliation_items_government ON reconciliation_items (government_reference);

CREATE TABLE reconciliation_exceptions (
    id               UUID PRIMARY KEY,
    batch_id         UUID           NOT NULL REFERENCES reconciliation_batches (id),
    item_id          UUID           NOT NULL REFERENCES reconciliation_items (id),
    exception_type   VARCHAR(64)    NOT NULL,
    description      VARCHAR(512),
    amount           NUMERIC(19, 2),
    currency         VARCHAR(3),
    status           VARCHAR(32)    NOT NULL DEFAULT 'OPEN',
    created_at       TIMESTAMPTZ    NOT NULL DEFAULT now(),
    resolved_at      TIMESTAMPTZ,
    resolved_by      VARCHAR(128),
    CONSTRAINT ck_reconciliation_exceptions_status CHECK (status IN ('OPEN', 'RESOLVED', 'IGNORED'))
);
CREATE INDEX idx_reconciliation_exceptions_batch ON reconciliation_exceptions (batch_id, status);
CREATE INDEX idx_reconciliation_exceptions_item ON reconciliation_exceptions (item_id);
