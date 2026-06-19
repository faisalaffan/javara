CREATE TABLE javara_t24.enrichment_rule (
    rule_id UUID PRIMARY KEY,
    rule_name VARCHAR(100) NOT NULL,
    channel VARCHAR(30),
    transaction_type VARCHAR(20),
    field_path VARCHAR(200),
    enrichment_source VARCHAR(30) NOT NULL,
    source_config JSONB,
    priority INTEGER NOT NULL DEFAULT 0,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
