package id.co.javara.app.service;

import id.co.javara.app.repository.AuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.time.Instant;

@Service
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);
    private final AuditLogRepository repository;

    public AuditService(AuditLogRepository repository) {
        this.repository = repository;
    }

    @Async
    public void log(String eventType, String adapter, String requestUrl,
                    String requestPayload, String responsePayload,
                    int httpStatus, long durationMs, String errorCode,
                    String correlationId) {
        repository.insert(eventType, adapter, requestUrl, requestPayload,
            responsePayload, httpStatus, durationMs, errorCode, correlationId, Instant.now());
    }
}
