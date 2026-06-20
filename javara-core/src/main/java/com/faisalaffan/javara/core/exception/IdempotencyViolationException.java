package com.faisalaffan.javara.core.exception;

public class IdempotencyViolationException extends JavaraException {

    public IdempotencyViolationException(String idempotencyKey, String detail) {
        super("IDEMPOTENCY_VIOLATION",
            "Key '" + idempotencyKey + "' reused with different payload: " + detail);
    }
}
