package id.co.javara.app.repository;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public class IdempotencyRegistryRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public IdempotencyRegistryRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<String> findResponseByKey(String key) {
        var result = jdbc.query("""
            SELECT response_payload, http_status
            FROM javara_t24.idempotency_registry
            WHERE idempotency_key = :key AND expires_at > NOW()
            """,
            new MapSqlParameterSource("key", key),
            rs -> {
                if (rs.next()) {
                    return rs.getString("response_payload");
                }
                return null;
            });
        return Optional.ofNullable(result);
    }

    public void insert(String key, String responsePayload, int httpStatus) {
        var params = new MapSqlParameterSource()
            .addValue("key", key)
            .addValue("response_payload", responsePayload)
            .addValue("http_status", httpStatus);
        jdbc.update("""
            INSERT INTO javara_t24.idempotency_registry
                (idempotency_key, response_payload, http_status)
            VALUES (:key, :response_payload::jsonb, :http_status)
            ON CONFLICT (idempotency_key) DO NOTHING
            """, params);
    }
}
