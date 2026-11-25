ALTER TABLE reconciliation_items ADD COLUMN government_amount NUMERIC(19, 2);

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
    CONSTRAINT ck_reconciliation_outbox_status CHECK (status IN ('PENDING', 'PUBLISHED', 'FAILED'))
);
CREATE INDEX idx_reconciliation_outbox_pending ON outbox_events (status, next_attempt_at);
