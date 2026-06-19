package id.co.javara.core.domain.vo;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class AmountTest {

    @Test
    void shouldCreateValidAmount() {
        var amount = new Amount(new BigDecimal("100.00"), "USD");
        assertThat(amount.value()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(amount.currency()).isEqualTo("USD");
        assertThat(amount.toString()).isEqualTo("100.00 USD");
    }

    @Test
    void shouldRejectNullValue() {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> new Amount(null, "USD"));
    }

    @Test
    void shouldRejectNullCurrency() {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> new Amount(new BigDecimal("100.00"), null));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "\t", "\n"})
    void shouldRejectBlankCurrency(String input) {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> new Amount(new BigDecimal("100.00"), input));
    }

    @Test
    void shouldRejectNonPositiveValue() {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> new Amount(BigDecimal.ZERO, "USD"));
        assertThatIllegalArgumentException()
            .isThrownBy(() -> new Amount(new BigDecimal("-1.00"), "USD"));
    }

    @Test
    void shouldBeEqualWhenSameValueAndCurrency() {
        var a1 = new Amount(new BigDecimal("100.00"), "USD");
        var a2 = new Amount(new BigDecimal("100.00"), "USD");
        assertThat(a1).isEqualTo(a2);
    }

    @Test
    void shouldNotBeEqualWhenDifferentCurrency() {
        var a1 = new Amount(new BigDecimal("100.00"), "USD");
        var a2 = new Amount(new BigDecimal("100.00"), "IDR");
        assertThat(a1).isNotEqualTo(a2);
    }
}
