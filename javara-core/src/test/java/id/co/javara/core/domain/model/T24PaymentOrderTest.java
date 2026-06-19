package id.co.javara.core.domain.model;

import id.co.javara.core.domain.vo.*;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

class T24PaymentOrderTest {

    @Test
    void shouldBuildPaymentOrderWithAllFields() {
        var po = T24PaymentOrder.builder()
            .transactionId(new IdempotencyKey("PO-001"))
            .debitAccount(new AccountNumber("11111-111-1-USD"))
            .creditAccount(new AccountNumber("22222-222-1-USD"))
            .amount(new Amount(new BigDecimal("15000000.00"), "USD"))
            .paymentDetails("Payment for invoice INV-001")
            .beneficiaryName("Jane Smith")
            .beneficiaryBank("BANK XYZ")
            .valueDate("2026-06-25")
            .build();

        assertThat(po.transactionId().value()).isEqualTo("PO-001");
        assertThat(po.debitAccount().value()).isEqualTo("11111-111-1-USD");
        assertThat(po.creditAccount().value()).isEqualTo("22222-222-1-USD");
        assertThat(po.amount().value()).isEqualByComparingTo(new BigDecimal("15000000.00"));
        assertThat(po.amount().currency()).isEqualTo("USD");
        assertThat(po.paymentDetails()).isEqualTo("Payment for invoice INV-001");
        assertThat(po.beneficiaryName()).isEqualTo("Jane Smith");
        assertThat(po.beneficiaryBank()).isEqualTo("BANK XYZ");
        assertThat(po.valueDate()).isEqualTo("2026-06-25");
    }

    @Test
    void shouldRejectMissingTransactionId() {
        assertThatIllegalStateException()
            .isThrownBy(() -> T24PaymentOrder.builder()
                .debitAccount(new AccountNumber("11111-111-1-USD"))
                .creditAccount(new AccountNumber("22222-222-1-USD"))
                .amount(new Amount(new BigDecimal("1000.00"), "USD"))
                .build())
            .withMessageContaining("transactionId");
    }

    @Test
    void shouldRejectMissingDebitAccount() {
        assertThatIllegalStateException()
            .isThrownBy(() -> T24PaymentOrder.builder()
                .transactionId(new IdempotencyKey("PO-001"))
                .creditAccount(new AccountNumber("22222-222-1-USD"))
                .amount(new Amount(new BigDecimal("1000.00"), "USD"))
                .build())
            .withMessageContaining("debitAccount");
    }

    @Test
    void shouldRejectMissingAmount() {
        assertThatIllegalStateException()
            .isThrownBy(() -> T24PaymentOrder.builder()
                .transactionId(new IdempotencyKey("PO-001"))
                .debitAccount(new AccountNumber("11111-111-1-USD"))
                .creditAccount(new AccountNumber("22222-222-1-USD"))
                .build())
            .withMessageContaining("amount");
    }
}
