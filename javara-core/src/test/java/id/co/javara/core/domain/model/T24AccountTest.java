package id.co.javara.core.domain.model;

import id.co.javara.core.domain.vo.AccountNumber;
import id.co.javara.core.domain.vo.CustomerId;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

class T24AccountTest {

    @Test
    void shouldBuildAccountWithAllFields() {
        var a = T24Account.builder()
            .accountNumber(new AccountNumber("12345-678-1-USD"))
            .customerId(new CustomerId("CUST.001"))
            .currency("USD")
            .accountType("SAVINGS")
            .balance(new BigDecimal("5000000.00"))
            .status("ACTIVE")
            .openedDate("2026-01-15")
            .build();

        assertThat(a.accountNumber().value()).isEqualTo("12345-678-1-USD");
        assertThat(a.customerId().value()).isEqualTo("CUST.001");
        assertThat(a.currency()).isEqualTo("USD");
        assertThat(a.accountType()).isEqualTo("SAVINGS");
        assertThat(a.balance()).isEqualByComparingTo(new BigDecimal("5000000.00"));
        assertThat(a.status()).isEqualTo("ACTIVE");
        assertThat(a.openedDate()).isEqualTo("2026-01-15");
    }

    @Test
    void shouldRejectMissingAccountNumber() {
        assertThatIllegalStateException()
            .isThrownBy(() -> T24Account.builder()
                .customerId(new CustomerId("CUST.001"))
                .currency("USD")
                .build())
            .withMessageContaining("accountNumber");
    }

    @Test
    void shouldRejectMissingCustomerId() {
        assertThatIllegalStateException()
            .isThrownBy(() -> T24Account.builder()
                .accountNumber(new AccountNumber("12345-678-1-USD"))
                .currency("USD")
                .build())
            .withMessageContaining("customerId");
    }

    @Test
    void shouldRejectMissingCurrency() {
        assertThatIllegalStateException()
            .isThrownBy(() -> T24Account.builder()
                .accountNumber(new AccountNumber("12345-678-1-USD"))
                .customerId(new CustomerId("CUST.001"))
                .build())
            .withMessageContaining("currency");
    }
}
