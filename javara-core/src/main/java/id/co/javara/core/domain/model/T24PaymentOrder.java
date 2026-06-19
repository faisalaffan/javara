package id.co.javara.core.domain.model;

import id.co.javara.core.domain.vo.AccountNumber;
import id.co.javara.core.domain.vo.Amount;
import id.co.javara.core.domain.vo.IdempotencyKey;

public record T24PaymentOrder(
    IdempotencyKey transactionId,
    AccountNumber debitAccount,
    AccountNumber creditAccount,
    Amount amount,
    String paymentDetails,
    String beneficiaryName,
    String beneficiaryBank,
    String valueDate
) {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private IdempotencyKey transactionId;
        private AccountNumber debitAccount;
        private AccountNumber creditAccount;
        private Amount amount;
        private String paymentDetails;
        private String beneficiaryName;
        private String beneficiaryBank;
        private String valueDate;

        public Builder transactionId(IdempotencyKey id) { this.transactionId = id; return this; }
        public Builder debitAccount(AccountNumber acct) { this.debitAccount = acct; return this; }
        public Builder creditAccount(AccountNumber acct) { this.creditAccount = acct; return this; }
        public Builder amount(Amount a) { this.amount = a; return this; }
        public Builder paymentDetails(String details) { this.paymentDetails = details; return this; }
        public Builder beneficiaryName(String name) { this.beneficiaryName = name; return this; }
        public Builder beneficiaryBank(String bank) { this.beneficiaryBank = bank; return this; }
        public Builder valueDate(String vd) { this.valueDate = vd; return this; }

        public T24PaymentOrder build() {
            if (transactionId == null) throw new IllegalStateException("transactionId is required");
            if (debitAccount == null) throw new IllegalStateException("debitAccount is required");
            if (creditAccount == null) throw new IllegalStateException("creditAccount is required");
            if (amount == null) throw new IllegalStateException("amount is required");
            return new T24PaymentOrder(
                transactionId, debitAccount, creditAccount, amount,
                paymentDetails, beneficiaryName, beneficiaryBank, valueDate
            );
        }
    }
}
