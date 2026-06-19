package id.co.javara.core.domain.vo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class AccountNumberTest {

    @Test
    void shouldCreateValidAccountNumber() {
        var account = new AccountNumber("12345-678-1-USD");
        assertThat(account.value()).isEqualTo("12345-678-1-USD");
        assertThat(account.toString()).isEqualTo("12345-678-1-USD");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "\t", "\n"})
    void shouldRejectBlankValue(String input) {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> new AccountNumber(input));
    }

    @Test
    void shouldBeEqualWhenSameValue() {
        var a1 = new AccountNumber("12345-678-1-USD");
        var a2 = new AccountNumber("12345-678-1-USD");
        assertThat(a1).isEqualTo(a2);
    }
}
