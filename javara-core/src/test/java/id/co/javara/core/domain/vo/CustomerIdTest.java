package id.co.javara.core.domain.vo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class CustomerIdTest {

    @Test
    void shouldCreateValidCustomerId() {
        var id = new CustomerId("CUST001");
        assertThat(id.value()).isEqualTo("CUST001");
        assertThat(id.toString()).isEqualTo("CUST001");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "\t", "\n"})
    void shouldRejectBlankValue(String input) {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> new CustomerId(input));
    }

    @Test
    void shouldBeEqualWhenSameValue() {
        var c1 = new CustomerId("CUST001");
        var c2 = new CustomerId("CUST001");
        assertThat(c1).isEqualTo(c2);
    }
}
