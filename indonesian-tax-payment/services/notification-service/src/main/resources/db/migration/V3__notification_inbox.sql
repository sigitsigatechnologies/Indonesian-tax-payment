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
