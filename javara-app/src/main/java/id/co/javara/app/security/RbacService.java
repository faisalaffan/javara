package id.co.javara.app.security;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class RbacService {

    private final NamedParameterJdbcTemplate jdbc;

    public RbacService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Set<String> getUserPermissions(UUID userId, String tenantId) {
        String sql = """
            SELECT DISTINCT p.code
            FROM javara_t24.user_role ur
            JOIN javara_t24.role_permission rp ON ur.role_id = rp.role_id
            JOIN javara_t24.permission p ON rp.permission_id = p.id
            WHERE ur.user_id = :userId
              AND (ur.tenant_id = :tenantId OR ur.tenant_id IS NULL)
            """;
        var params = new MapSqlParameterSource()
            .addValue("userId", userId)
            .addValue("tenantId", tenantId);
        List<String> permissions = jdbc.queryForList(sql, params, String.class);
        return new HashSet<>(permissions);
    }

    public boolean hasPermission(UUID userId, String tenantId, String permissionCode) {
        return getUserPermissions(userId, tenantId).contains(permissionCode);
    }

    public void assignRole(UUID userId, String roleCode, String tenantId, String grantedBy) {
        String sql = """
            INSERT INTO javara_t24.user_role (user_id, role_id, tenant_id, granted_by)
            SELECT :userId, r.id, :tenantId, :grantedBy
            FROM javara_t24.role r WHERE r.code = :roleCode
            ON CONFLICT (user_id, role_id, tenant_id) DO NOTHING
            """;
        var params = new MapSqlParameterSource()
            .addValue("userId", userId)
            .addValue("roleCode", roleCode)
            .addValue("tenantId", tenantId)
            .addValue("grantedBy", grantedBy);
        jdbc.update(sql, params);
    }

    public void revokeRole(UUID userId, String roleCode, String tenantId) {
        String sql = """
            DELETE FROM javara_t24.user_role
            WHERE user_id = :userId
              AND role_id = (SELECT id FROM javara_t24.role WHERE code = :roleCode)
              AND tenant_id = :tenantId
            """;
        var params = new MapSqlParameterSource()
            .addValue("userId", userId)
            .addValue("roleCode", roleCode)
            .addValue("tenantId", tenantId);
        jdbc.update(sql, params);
    }
}
