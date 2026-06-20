package com.faisalaffan.javara.core.domain.vo;

public record CustomerId(String value) {

    public CustomerId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Customer ID must not be null or blank");
        }
    }

    @Override
    public String toString() {
        return value;
    }
}
