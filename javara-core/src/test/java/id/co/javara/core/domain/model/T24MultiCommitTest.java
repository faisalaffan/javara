package id.co.javara.core.domain.model;

import id.co.javara.core.domain.vo.*;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

class T24MultiCommitTest {

    @Test
    void shouldBuildMultiCommitWithTransactions() {
        var tx1 = T24FundTransfer.builder()
            .transactionId(new IdempotencyKey("uuid-001"))
            .debitAccount(new AccountNumber("11111-111-1-USD"))
            .creditAccount(new AccountNumber("22222-222-1-USD"))
            .amount(new Amount(new BigDecimal("100000.00"), "USD"))
            .build();

        var tx2 = T24FundTransfer.builder()
            .transactionId(new IdempotencyKey("uuid-002"))
            .debitAccount(new AccountNumber("33333-333-1-USD"))
            .creditAccount(new AccountNumber("44444-444-1-USD"))
            .amount(new Amount(new BigDecimal("200000.00"), "USD"))
            .build();

        var mc = T24MultiCommit.builder()
            .batchId("BATCH-20260620-001")
            .transactions(List.of(tx1, tx2))
            .commitMode("ATOMIC")
            .build();

        assertThat(mc.batchId()).isEqualTo("BATCH-20260620-001");
        assertThat(mc.transactions()).hasSize(2);
        assertThat(mc.transactions().get(0).transactionId().value()).isEqualTo("uuid-001");
        assertThat(mc.transactions().get(1).transactionId().value()).isEqualTo("uuid-002");
        assertThat(mc.commitMode()).isEqualTo("ATOMIC");
    }

    @Test
    void shouldRejectMissingBatchId() {
        assertThatIllegalStateException()
            .isThrownBy(() -> T24MultiCommit.builder()
                .transactions(List.of())
                .commitMode("ATOMIC")
                .build())
            .withMessageContaining("batchId");
    }

    @Test
    void shouldRejectMissingTransactions() {
        assertThatIllegalStateException()
            .isThrownBy(() -> T24MultiCommit.builder()
                .batchId("BATCH-001")
                .commitMode("ATOMIC")
                .build())
            .withMessageContaining("transactions");
    }
}
