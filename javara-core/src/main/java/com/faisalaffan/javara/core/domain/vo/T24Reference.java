package com.faisalaffan.javara.core.domain.vo;

public record T24Reference(String value) {

    public T24Reference {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("T24 reference must not be null or blank");
        }
    }

    @Override
    public String toString() {
        return value;
    }
}
