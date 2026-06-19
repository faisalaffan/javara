CREATE TABLE javara_t24.customer_cache (
    customer_id VARCHAR(36) PRIMARY KEY,
    cif_number VARCHAR(50) NOT NULL,
    full_name VARCHAR(200),
    id_type VARCHAR(10),
    id_number VARCHAR(50),
    branch_code VARCHAR(10),
    status VARCHAR(20),
    raw_json JSONB,
    cached_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    expires_at TIMESTAMPTZ NOT NULL DEFAULT NOW() + INTERVAL '5 minutes'
);

CREATE INDEX idx_customer_cache_cif ON javara_t24.customer_cache(cif_number);
CREATE INDEX idx_customer_cache_id ON javara_t24.customer_cache(id_type, id_number);
