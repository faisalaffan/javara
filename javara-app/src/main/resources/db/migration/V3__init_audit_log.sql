CREATE TABLE javara_t24.audit_log (
    id BIGSERIAL,
    event_type VARCHAR(50) NOT NULL,
    adapter VARCHAR(20),
    request_url TEXT,
    request_payload JSONB,
    response_payload JSONB,
    http_status INTEGER,
    duration_ms BIGINT,
    error_code VARCHAR(50),
    caller_ip VARCHAR(45),
    correlation_id VARCHAR(36),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (id, created_at)
) PARTITION BY RANGE (created_at);

CREATE INDEX idx_audit_correlation ON javara_t24.audit_log(correlation_id, created_at);
