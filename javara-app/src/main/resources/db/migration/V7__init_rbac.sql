CREATE TABLE IF NOT EXISTS javara_t24.role (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS javara_t24.permission (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    resource_type VARCHAR(50) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS javara_t24.role_permission (
    role_id UUID NOT NULL REFERENCES javara_t24.role(id),
    permission_id UUID NOT NULL REFERENCES javara_t24.permission(id),
    PRIMARY KEY (role_id, permission_id)
);

CREATE TABLE IF NOT EXISTS javara_t24.user_role (
    user_id UUID NOT NULL,
    role_id UUID NOT NULL REFERENCES javara_t24.role(id),
    tenant_id VARCHAR(20),
    granted_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    granted_by VARCHAR(100),
    PRIMARY KEY (user_id, role_id, tenant_id)
);

-- Seed permissions
INSERT INTO javara_t24.permission (code, description, resource_type) VALUES
    ('t24:customer:read', 'Read customer data', 'CUSTOMER'),
    ('t24:customer:write', 'Create/update customer data', 'CUSTOMER'),
    ('t24:transaction:ft', 'Post fund transfer', 'TRANSACTION'),
    ('t24:transaction:teller', 'Post teller transaction', 'TRANSACTION'),
    ('t24:transaction:po', 'Post payment order', 'TRANSACTION'),
    ('t24:transaction:multi-commit', 'Post multi-commit', 'TRANSACTION'),
    ('t24:enquiry:all', 'All enquiry operations', 'ENQUIRY')
ON CONFLICT (code) DO NOTHING;

-- Seed default roles
INSERT INTO javara_t24.role (code, name, description) VALUES
    ('T24_ADMIN', 'T24 Administrator', 'Full access to all T24 operations'),
    ('T24_OPERATOR', 'T24 Operator', 'Transaction posting and customer inquiry'),
    ('T24_VIEWER', 'T24 Viewer', 'Read-only access')
ON CONFLICT (code) DO NOTHING;

-- Assign all permissions to ADMIN
INSERT INTO javara_t24.role_permission (role_id, permission_id)
SELECT r.id, p.id FROM javara_t24.role r, javara_t24.permission p
WHERE r.code = 'T24_ADMIN'
ON CONFLICT DO NOTHING;

-- Assign read + basic tx to OPERATOR
INSERT INTO javara_t24.role_permission (role_id, permission_id)
SELECT r.id, p.id FROM javara_t24.role r, javara_t24.permission p
WHERE r.code = 'T24_OPERATOR' AND p.code IN (
    't24:customer:read', 't24:transaction:ft', 't24:transaction:teller', 't24:enquiry:all'
) ON CONFLICT DO NOTHING;

-- Assign read-only to VIEWER
INSERT INTO javara_t24.role_permission (role_id, permission_id)
SELECT r.id, p.id FROM javara_t24.role r, javara_t24.permission p
WHERE r.code = 'T24_VIEWER' AND p.code IN ('t24:customer:read', 't24:enquiry:all')
ON CONFLICT DO NOTHING;
