package id.co.javara.core.domain.model;

import id.co.javara.core.domain.vo.*;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

class T24TellerTransactionTest {

    @Test
    void shouldBuildTellerTransactionWithAllFields() {
        var tt = T24TellerTransaction.builder()
            .transactionId(new IdempotencyKey("TELLER-001"))
            .accountNumber(new AccountNumber("12345-678-1-USD"))
            .amount(new Amount(new BigDecimal("500000.00"), "USD"))
            .transactionCode("WD01")
            .tellerId("TELLER-JOHN")
            .branchCode("JKT001")
            .narration("Withdrawal for operational expenses")
            .build();

        assertThat(tt.transactionId().value()).isEqualTo("TELLER-001");
        assertThat(tt.accountNumber().value()).isEqualTo("12345-678-1-USD");
        assertThat(tt.amount().value()).isEqualByComparingTo(new BigDecimal("500000.00"));
        assertThat(tt.amount().currency()).isEqualTo("USD");
        assertThat(tt.transactionCode()).isEqualTo("WD01");
        assertThat(tt.tellerId()).isEqualTo("TELLER-JOHN");
        assertThat(tt.branchCode()).isEqualTo("JKT001");
        assertThat(tt.narration()).isEqualTo("Withdrawal for operational expenses");
    }

    @Test
    void shouldRejectMissingTransactionId() {
        assertThatIllegalStateException()
            .isThrownBy(() -> T24TellerTransaction.builder()
                .accountNumber(new AccountNumber("12345-678-1-USD"))
                .amount(new Amount(new BigDecimal("500000.00"), "USD"))
                .transactionCode("WD01")
                .tellerId("TELLER-JOHN")
                .branchCode("JKT001")
                .build())
            .withMessageContaining("transactionId");
    }

    @Test
    void shouldRejectMissingAccountNumber() {
        assertThatIllegalStateException()
            .isThrownBy(() -> T24TellerTransaction.builder()
                .transactionId(new IdempotencyKey("TELLER-001"))
                .amount(new Amount(new BigDecimal("500000.00"), "USD"))
                .transactionCode("WD01")
                .tellerId("TELLER-JOHN")
                .branchCode("JKT001")
                .build())
            .withMessageContaining("accountNumber");
    }

    @Test
    void shouldRejectMissingAmount() {
        assertThatIllegalStateException()
            .isThrownBy(() -> T24TellerTransaction.builder()
                .transactionId(new IdempotencyKey("TELLER-001"))
                .accountNumber(new AccountNumber("12345-678-1-USD"))
                .transactionCode("WD01")
                .tellerId("TELLER-JOHN")
                .branchCode("JKT001")
                .build())
            .withMessageContaining("amount");
    }
}
