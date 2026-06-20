package com.faisalaffan.javara.core.domain.vo;

import java.util.UUID;

public record IdempotencyKey(String value) {

    public IdempotencyKey {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Idempotency key must not be null or blank");
        }
    }

    public static IdempotencyKey generate() {
        return new IdempotencyKey(UUID.randomUUID().toString());
    }

    @Override
    public String toString() {
        return value;
    }
}
