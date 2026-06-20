package com.faisalaffan.javara.app.config;

import com.faisalaffan.javara.app.dto.ErrorResponse;
import com.faisalaffan.javara.core.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.time.Instant;
import java.util.UUID;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(T24BusinessException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ErrorResponse handleBusiness(T24BusinessException ex) {
        return new ErrorResponse("T24_BUSINESS_ERROR", ex.getMessage(),
            ex.t24ErrorCode(), UUID.randomUUID().toString(), Instant.now());
    }

    @ExceptionHandler(T24ConnectionException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public ErrorResponse handleConnection(T24ConnectionException ex) {
        return new ErrorResponse(ex.errorCode(), ex.getMessage(),
            null, UUID.randomUUID().toString(), Instant.now());
    }

    @ExceptionHandler(T24TimeoutException.class)
    @ResponseStatus(HttpStatus.GATEWAY_TIMEOUT)
    public ErrorResponse handleTimeout(T24TimeoutException ex) {
        return new ErrorResponse("T24_TIMEOUT", ex.getMessage(),
            null, UUID.randomUUID().toString(), Instant.now());
    }

    @ExceptionHandler(IdempotencyViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleIdempotency(IdempotencyViolationException ex) {
        return new ErrorResponse("IDEMPOTENCY_VIOLATION", ex.getMessage(),
            null, UUID.randomUUID().toString(), Instant.now());
    }
}
