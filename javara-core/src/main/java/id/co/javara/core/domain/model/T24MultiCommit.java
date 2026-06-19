package id.co.javara.core.domain.model;

import java.util.List;

public record T24MultiCommit(
    String batchId,
    List<T24FundTransfer> transactions,
    String commitMode
) {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String batchId;
        private List<T24FundTransfer> transactions;
        private String commitMode;

        public Builder batchId(String id) { this.batchId = id; return this; }
        public Builder transactions(List<T24FundTransfer> txs) { this.transactions = txs; return this; }
        public Builder commitMode(String cm) { this.commitMode = cm; return this; }

        public T24MultiCommit build() {
            if (batchId == null || batchId.isBlank()) throw new IllegalStateException("batchId is required");
            if (transactions == null) throw new IllegalStateException("transactions is required");
            return new T24MultiCommit(batchId, transactions, commitMode);
        }
    }
}
