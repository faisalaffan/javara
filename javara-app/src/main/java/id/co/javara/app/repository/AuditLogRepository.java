package id.co.javara.app.repository;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import java.time.Instant;

@Repository
public class AuditLogRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public AuditLogRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void insert(String eventType, String adapter, String requestUrl,
                       String requestPayload, String responsePayload,
                       int httpStatus, long durationMs, String errorCode,
                       String correlationId, Instant createdAt) {
        var params = new MapSqlParameterSource()
            .addValue("event_type", eventType)
            .addValue("adapter", adapter)
            .addValue("request_url", requestUrl)
            .addValue("request_payload", requestPayload)
            .addValue("response_payload", responsePayload)
            .addValue("http_status", httpStatus)
            .addValue("duration_ms", durationMs)
            .addValue("error_code", errorCode)
            .addValue("correlation_id", correlationId)
            .addValue("created_at", createdAt);

        jdbc.update("""
            INSERT INTO javara_t24.audit_log
                (event_type, adapter, request_url, request_payload,
                 response_payload, http_status, duration_ms,
                 error_code, correlation_id, created_at)
            VALUES
                (:event_type, :adapter, :request_url, :request_payload::jsonb,
                 :response_payload::jsonb, :http_status, :duration_ms,
                 :error_code, :correlation_id, :created_at)
            """, params);
    }
}
