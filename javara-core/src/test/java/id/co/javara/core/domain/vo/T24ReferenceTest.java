package id.co.javara.core.domain.vo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class T24ReferenceTest {

    @Test
    void shouldCreateValidT24Reference() {
        var ref = new T24Reference("FT20250620.001");
        assertThat(ref.value()).isEqualTo("FT20250620.001");
        assertThat(ref.toString()).isEqualTo("FT20250620.001");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "\t", "\n"})
    void shouldRejectBlankValue(String input) {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> new T24Reference(input));
    }

    @Test
    void shouldBeEqualWhenSameValue() {
        var r1 = new T24Reference("FT20250620.001");
        var r2 = new T24Reference("FT20250620.001");
        assertThat(r1).isEqualTo(r2);
    }
}
