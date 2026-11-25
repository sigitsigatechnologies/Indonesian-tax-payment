CREATE TABLE notification_templates (
    id             UUID PRIMARY KEY,
    template_code  VARCHAR(64)  NOT NULL,
    channel        VARCHAR(32)  NOT NULL,
    subject        VARCHAR(255),
    body           TEXT         NOT NULL,
    active         BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uk_notification_templates_code UNIQUE (template_code)
);

CREATE TABLE notification_requests (
    id               UUID PRIMARY KEY,
    notification_id  VARCHAR(64)  NOT NULL,
    payment_id       UUID         NOT NULL,
    channel          VARCHAR(32)  NOT NULL,
    recipient        VARCHAR(255) NOT NULL,
    template_code    VARCHAR(64)  NOT NULL,
    status           VARCHAR(32)  NOT NULL,
    payload          JSONB        NOT NULL,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    version          BIGINT       NOT NULL DEFAULT 0,
    CONSTRAINT uk_notification_requests_notification UNIQUE (notification_id),
    CONSTRAINT ck_notification_requests_status
        CHECK (status IN ('PENDING', 'SENT', 'FAILED', 'RETRYING'))
);
CREATE INDEX idx_notification_requests_payment ON notification_requests (payment_id);
CREATE INDEX idx_notification_requests_status ON notification_requests (status, created_at);

CREATE TABLE notification_attempts (
    id               UUID PRIMARY KEY,
    notification_id  VARCHAR(64) NOT NULL,
    attempt_number   INTEGER     NOT NULL,
    status           VARCHAR(32) NOT NULL,
    http_status      INTEGER,
    error_message    VARCHAR(512),
    attempted_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uk_notification_attempts_number UNIQUE (notification_id, attempt_number),
    CONSTRAINT fk_notification_attempts_request FOREIGN KEY (notification_id)
        REFERENCES notification_requests (notification_id)
);
CREATE INDEX idx_notification_attempts_request ON notification_attempts (notification_id, attempted_at DESC);
