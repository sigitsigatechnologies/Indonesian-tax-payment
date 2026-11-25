CREATE TABLE bank_accounts (
    id                 UUID PRIMARY KEY,
    account_number     VARCHAR(34)    NOT NULL,
    account_name       VARCHAR(255)   NOT NULL,
    customer_id        VARCHAR(64)    NOT NULL,
    currency           VARCHAR(3)        NOT NULL,
    available_balance  NUMERIC(19, 2) NOT NULL CHECK (available_balance >= 0),
    ledger_balance     NUMERIC(19, 2) NOT NULL CHECK (ledger_balance >= 0),
    status             VARCHAR(32)    NOT NULL,
    version            BIGINT         NOT NULL DEFAULT 0,
    created_at         TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at         TIMESTAMPTZ    NOT NULL DEFAULT now(),
    CONSTRAINT uk_bank_accounts_number UNIQUE (account_number),
    CONSTRAINT ck_bank_accounts_status CHECK (status IN ('ACTIVE', 'BLOCKED', 'CLOSED'))
);
CREATE INDEX idx_bank_accounts_customer ON bank_accounts (customer_id);

CREATE TABLE bank_transactions (
    id                     UUID PRIMARY KEY,
    transaction_reference  VARCHAR(64)    NOT NULL,
    account_number         VARCHAR(34)    NOT NULL,
    transaction_type       VARCHAR(32)    NOT NULL,
    amount                 NUMERIC(19, 2) NOT NULL CHECK (amount > 0),
    currency               VARCHAR(3)        NOT NULL,
    status                 VARCHAR(32)    NOT NULL,
    balance_after          NUMERIC(19, 2),
    description            VARCHAR(255),
    created_at             TIMESTAMPTZ    NOT NULL DEFAULT now(),
    completed_at           TIMESTAMPTZ,
    version                BIGINT         NOT NULL DEFAULT 0,
    CONSTRAINT uk_bank_transactions_reference UNIQUE (transaction_reference),
    CONSTRAINT fk_bank_transactions_account FOREIGN KEY (account_number)
        REFERENCES bank_accounts (account_number)
);
CREATE INDEX idx_bank_transactions_account ON bank_transactions (account_number, created_at DESC);

CREATE TABLE bank_reversal_transactions (
    id                             UUID PRIMARY KEY,
    reversal_reference             VARCHAR(64)    NOT NULL,
    original_transaction_reference VARCHAR(64)    NOT NULL,
    account_number                 VARCHAR(34)    NOT NULL,
    amount                         NUMERIC(19, 2) NOT NULL CHECK (amount > 0),
    currency                       VARCHAR(3)        NOT NULL,
    status                         VARCHAR(32)    NOT NULL,
    reason                         VARCHAR(255),
    created_at                     TIMESTAMPTZ    NOT NULL DEFAULT now(),
    completed_at                   TIMESTAMPTZ,
    CONSTRAINT uk_bank_reversal_transactions_reference UNIQUE (reversal_reference),
    CONSTRAINT uk_bank_reversal_transactions_original UNIQUE (original_transaction_reference),
    CONSTRAINT fk_bank_reversal_transactions_account FOREIGN KEY (account_number)
        REFERENCES bank_accounts (account_number)
);
