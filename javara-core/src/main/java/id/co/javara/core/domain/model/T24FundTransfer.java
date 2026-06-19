package id.co.javara.core.domain.model;

import id.co.javara.core.domain.vo.AccountNumber;
import id.co.javara.core.domain.vo.Amount;
import id.co.javara.core.domain.vo.IdempotencyKey;
import id.co.javara.core.domain.vo.T24Reference;
import java.time.Instant;

public record T24FundTransfer(
    IdempotencyKey transactionId,
    AccountNumber debitAccount,
    AccountNumber creditAccount,
    Amount amount,
    String paymentDetails,
    String channel,
    String valueDate,
    String processingPriority,
    String chargeCode,
    T24Reference t24Reference,
    TransactionStatus status,
    Instant postedAt
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
        private String channel;
        private String valueDate;
        private String processingPriority = "NORMAL";
        private String chargeCode = "SHA";
        private T24Reference t24Reference;
        private TransactionStatus status = TransactionStatus.PENDING;
        private Instant postedAt;

        public Builder transactionId(IdempotencyKey id) { this.transactionId = id; return this; }
        public Builder debitAccount(AccountNumber acct) { this.debitAccount = acct; return this; }
        public Builder creditAccount(AccountNumber acct) { this.creditAccount = acct; return this; }
        public Builder amount(Amount a) { this.amount = a; return this; }
        public Builder paymentDetails(String details) { this.paymentDetails = details; return this; }
        public Builder channel(String ch) { this.channel = ch; return this; }
        public Builder valueDate(String vd) { this.valueDate = vd; return this; }
        public Builder processingPriority(String pp) { this.processingPriority = pp; return this; }
        public Builder chargeCode(String cc) { this.chargeCode = cc; return this; }
        public Builder t24Reference(T24Reference ref) { this.t24Reference = ref; return this; }
        public Builder status(TransactionStatus s) { this.status = s; return this; }
        public Builder postedAt(Instant at) { this.postedAt = at; return this; }

        public T24FundTransfer build() {
            if (transactionId == null) throw new IllegalStateException("transactionId is required");
            if (debitAccount == null) throw new IllegalStateException("debitAccount is required");
            if (creditAccount == null) throw new IllegalStateException("creditAccount is required");
            if (amount == null) throw new IllegalStateException("amount is required");
            return new T24FundTransfer(
                transactionId, debitAccount, creditAccount, amount,
                paymentDetails, channel, valueDate, processingPriority,
                chargeCode, t24Reference, status, postedAt
            );
        }
    }
}
