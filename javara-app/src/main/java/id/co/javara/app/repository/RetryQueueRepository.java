package id.co.javara.app.repository;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Map;

@Repository
public class RetryQueueRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public RetryQueueRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Map<String, Object>> findPendingRetries() {
        return jdbc.queryForList("""
            SELECT id, original_request, adapter, attempts, max_attempts
            FROM javara_t24.retry_queue
            WHERE status IN ('PENDING', 'RETRYING')
              AND next_retry_at <= NOW()
            ORDER BY next_retry_at
            LIMIT 50
            """, new MapSqlParameterSource());
    }
}
