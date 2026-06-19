package id.co.javara.core.domain.vo;

public record AccountNumber(String value) {

    public AccountNumber {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Account number must not be null or blank");
        }
    }

    @Override
    public String toString() {
        return value;
    }
}
