package id.co.javara.core.domain.model;

import id.co.javara.core.domain.vo.AccountNumber;
import id.co.javara.core.domain.vo.CustomerId;
import java.math.BigDecimal;

public record T24Account(
    AccountNumber accountNumber,
    CustomerId customerId,
    String currency,
    String accountType,
    BigDecimal balance,
    String status,
    String openedDate
) {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private AccountNumber accountNumber;
        private CustomerId customerId;
        private String currency;
        private String accountType;
        private BigDecimal balance;
        private String status;
        private String openedDate;

        public Builder accountNumber(AccountNumber acct) { this.accountNumber = acct; return this; }
        public Builder customerId(CustomerId id) { this.customerId = id; return this; }
        public Builder currency(String c) { this.currency = c; return this; }
        public Builder accountType(String type) { this.accountType = type; return this; }
        public Builder balance(BigDecimal bal) { this.balance = bal; return this; }
        public Builder status(String s) { this.status = s; return this; }
        public Builder openedDate(String od) { this.openedDate = od; return this; }

        public T24Account build() {
            if (accountNumber == null) throw new IllegalStateException("accountNumber is required");
            if (customerId == null) throw new IllegalStateException("customerId is required");
            if (currency == null || currency.isBlank()) throw new IllegalStateException("currency is required");
            return new T24Account(
                accountNumber, customerId, currency, accountType,
                balance, status, openedDate
            );
        }
    }
}
