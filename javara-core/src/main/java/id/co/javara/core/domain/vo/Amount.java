package id.co.javara.core.domain.vo;

import java.math.BigDecimal;

public record Amount(BigDecimal value, String currency) {

    public Amount {
        if (value == null) {
            throw new IllegalArgumentException("Amount value must not be null");
        }
        if (currency == null || currency.isBlank()) {
            throw new IllegalArgumentException("Currency must not be null or blank");
        }
        if (value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive: " + value);
        }
    }

    @Override
    public String toString() {
        return value + " " + currency;
    }
}
