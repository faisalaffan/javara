CREATE TABLE javara_t24.transaction_log (
    transaction_id VARCHAR(36) PRIMARY KEY,
    transaction_type VARCHAR(20) NOT NULL,
    channel VARCHAR(30),
    amount NUMERIC(22,2),
    currency VARCHAR(3),
    debit_account VARCHAR(50),
    credit_account VARCHAR(50),
    t24_reference VARCHAR(50),
    status VARCHAR(20) NOT NULL,
    request_payload JSONB,
    response_payload JSONB,
    idempotency_key VARCHAR(36),
    adapter_used VARCHAR(20),
    duration_ms BIGINT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_tx_log_t24ref ON javara_t24.transaction_log(t24_reference);
CREATE INDEX idx_tx_log_created ON javara_t24.transaction_log(created_at DESC);
