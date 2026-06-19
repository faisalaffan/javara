CREATE TYPE javara_t24.retry_status AS ENUM ('PENDING', 'RETRYING', 'EXHAUSTED', 'CANCELLED');

CREATE TABLE javara_t24.retry_queue (
    id UUID PRIMARY KEY,
    original_request JSONB NOT NULL,
    adapter VARCHAR(20) NOT NULL,
    attempts INTEGER NOT NULL DEFAULT 0,
    max_attempts INTEGER NOT NULL DEFAULT 5,
    last_error TEXT,
    next_retry_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    status javara_t24.retry_status NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_retry_queue_next ON javara_t24.retry_queue(status, next_retry_at)
    WHERE status IN ('PENDING', 'RETRYING');
