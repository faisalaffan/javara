package com.faisalaffan.javara.core.domain.model;

import com.faisalaffan.javara.core.domain.vo.AccountNumber;
import com.faisalaffan.javara.core.domain.vo.Amount;
import com.faisalaffan.javara.core.domain.vo.IdempotencyKey;

public record T24TellerTransaction(
    IdempotencyKey transactionId,
    AccountNumber accountNumber,
    Amount amount,
    String transactionCode,
    String tellerId,
    String branchCode,
    String narration
) {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private IdempotencyKey transactionId;
        private AccountNumber accountNumber;
        private Amount amount;
        private String transactionCode;
        private String tellerId;
        private String branchCode;
        private String narration;

        public Builder transactionId(IdempotencyKey id) { this.transactionId = id; return this; }
        public Builder accountNumber(AccountNumber acct) { this.accountNumber = acct; return this; }
        public Builder amount(Amount a) { this.amount = a; return this; }
        public Builder transactionCode(String tc) { this.transactionCode = tc; return this; }
        public Builder tellerId(String ti) { this.tellerId = ti; return this; }
        public Builder branchCode(String bc) { this.branchCode = bc; return this; }
        public Builder narration(String n) { this.narration = n; return this; }

        public T24TellerTransaction build() {
            if (transactionId == null) throw new IllegalStateException("transactionId is required");
            if (accountNumber == null) throw new IllegalStateException("accountNumber is required");
            if (amount == null) throw new IllegalStateException("amount is required");
            return new T24TellerTransaction(
                transactionId, accountNumber, amount, transactionCode,
                tellerId, branchCode, narration
            );
        }
    }
}
