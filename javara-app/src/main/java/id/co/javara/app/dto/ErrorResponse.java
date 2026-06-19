package id.co.javara.app.dto;

import java.time.Instant;

public record ErrorResponse(
    String errorCode,
    String message,
    String t24ErrorCode,
    String requestId,
    Instant timestamp
) {}
