package id.co.javara.core.domain.model;

import id.co.javara.core.domain.vo.*;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.Instant;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

class T24FundTransferTest {

    @Test
    void shouldBuildFundTransferWithAllFields() {
        var now = Instant.now();
        var ft = T24FundTransfer.builder()
            .transactionId(new IdempotencyKey("uuid-123"))
            .debitAccount(new AccountNumber("12345-678-1-USD"))
            .creditAccount(new AccountNumber("98765-432-1-USD"))
            .amount(new Amount(new BigDecimal("250000.00"), "USD"))
            .paymentDetails("Invoice INV-2026")
            .channel("INTERNET_BANKING")
            .valueDate("2026-06-20")
            .processingPriority("NORMAL")
            .chargeCode("SHA")
            .t24Reference(new T24Reference("FT20260620.001"))
            .status(TransactionStatus.PENDING)
            .postedAt(now)
            .build();

        assertThat(ft.transactionId().value()).isEqualTo("uuid-123");
        assertThat(ft.debitAccount().value()).isEqualTo("12345-678-1-USD");
        assertThat(ft.creditAccount().value()).isEqualTo("98765-432-1-USD");
        assertThat(ft.amount().value()).isEqualByComparingTo(new BigDecimal("250000.00"));
        assertThat(ft.amount().currency()).isEqualTo("USD");
        assertThat(ft.paymentDetails()).isEqualTo("Invoice INV-2026");
        assertThat(ft.channel()).isEqualTo("INTERNET_BANKING");
        assertThat(ft.valueDate()).isEqualTo("2026-06-20");
        assertThat(ft.processingPriority()).isEqualTo("NORMAL");
        assertThat(ft.chargeCode()).isEqualTo("SHA");
        assertThat(ft.t24Reference().value()).isEqualTo("FT20260620.001");
        assertThat(ft.status()).isEqualTo(TransactionStatus.PENDING);
        assertThat(ft.postedAt()).isEqualTo(now);
    }

    @Test
    void shouldApplyDefaults() {
        var ft = T24FundTransfer.builder()
            .transactionId(new IdempotencyKey("uuid-456"))
            .debitAccount(new AccountNumber("11111-111-1-USD"))
            .creditAccount(new AccountNumber("22222-222-1-USD"))
            .amount(new Amount(new BigDecimal("1000.00"), "USD"))
            .build();

        assertThat(ft.processingPriority()).isEqualTo("NORMAL");
        assertThat(ft.chargeCode()).isEqualTo("SHA");
        assertThat(ft.status()).isEqualTo(TransactionStatus.PENDING);
    }

    @Test
    void shouldRejectMissingTransactionId() {
        assertThatIllegalStateException()
            .isThrownBy(() -> T24FundTransfer.builder()
                .debitAccount(new AccountNumber("12345-678-1-USD"))
                .creditAccount(new AccountNumber("98765-432-1-USD"))
                .amount(new Amount(new BigDecimal("250000.00"), "USD"))
                .build())
            .withMessageContaining("transactionId");
    }

    @Test
    void shouldRejectMissingDebitAccount() {
        assertThatIllegalStateException()
            .isThrownBy(() -> T24FundTransfer.builder()
                .transactionId(new IdempotencyKey("uuid-123"))
                .creditAccount(new AccountNumber("98765-432-1-USD"))
                .amount(new Amount(new BigDecimal("250000.00"), "USD"))
                .build())
            .withMessageContaining("debitAccount");
    }

    @Test
    void shouldRejectMissingCreditAccount() {
        assertThatIllegalStateException()
            .isThrownBy(() -> T24FundTransfer.builder()
                .transactionId(new IdempotencyKey("uuid-123"))
                .debitAccount(new AccountNumber("12345-678-1-USD"))
                .amount(new Amount(new BigDecimal("250000.00"), "USD"))
                .build())
            .withMessageContaining("creditAccount");
    }

    @Test
    void shouldRejectMissingAmount() {
        assertThatIllegalStateException()
            .isThrownBy(() -> T24FundTransfer.builder()
                .transactionId(new IdempotencyKey("uuid-123"))
                .debitAccount(new AccountNumber("12345-678-1-USD"))
                .creditAccount(new AccountNumber("98765-432-1-USD"))
                .build())
            .withMessageContaining("amount");
    }
}
