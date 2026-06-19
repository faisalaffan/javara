package id.co.javara.core.domain.vo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class IdempotencyKeyTest {

    @Test
    void shouldCreateValidIdempotencyKey() {
        var key = new IdempotencyKey("txn-abc-123");
        assertThat(key.value()).isEqualTo("txn-abc-123");
        assertThat(key.toString()).isEqualTo("txn-abc-123");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "\t", "\n"})
    void shouldRejectBlankValue(String input) {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> new IdempotencyKey(input));
    }

    @Test
    void shouldBeEqualWhenSameValue() {
        var k1 = new IdempotencyKey("txn-abc-123");
        var k2 = new IdempotencyKey("txn-abc-123");
        assertThat(k1).isEqualTo(k2);
    }

    @Test
    void shouldGenerateRandomKey() {
        var key1 = IdempotencyKey.generate();
        var key2 = IdempotencyKey.generate();
        assertThat(key1.value()).isNotBlank();
        assertThat(key2.value()).isNotBlank();
        assertThat(key1).isNotEqualTo(key2);
    }
}
