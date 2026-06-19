CREATE SCHEMA IF NOT EXISTS javara_t24;

CREATE TABLE javara_t24.idempotency_registry (
    idempotency_key VARCHAR(36) PRIMARY KEY,
    response_payload JSONB NOT NULL,
    http_status INTEGER NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    expires_at TIMESTAMPTZ NOT NULL DEFAULT NOW() + INTERVAL '24 hours'
);

CREATE INDEX idx_idempotency_expires ON javara_t24.idempotency_registry(expires_at);
