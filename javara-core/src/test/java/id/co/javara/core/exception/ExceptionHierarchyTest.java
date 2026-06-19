package id.co.javara.core.exception;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class ExceptionHierarchyTest {

    @Test
    void t24BusinessExceptionShouldBeInstanceOfAllParents() {
        var ex = new T24BusinessException("EB-AC.DORMANT", "Account is dormant");
        assertThat(ex).isInstanceOf(T24ResponseException.class)
                      .isInstanceOf(JavaraException.class)
                      .isInstanceOf(RuntimeException.class);
        assertThat(ex.t24ErrorCode()).isEqualTo("EB-AC.DORMANT");
        assertThat(ex.errorCode()).isEqualTo("T24_RESPONSE_ERROR");
    }

    @Test
    void t24ConnectionExceptionShouldCarryMessage() {
        var ex = new T24ConnectionException("Connection refused",
                    new java.net.ConnectException("localhost:9443"));
        assertThat(ex.getMessage()).contains("Connection refused");
        assertThat(ex.errorCode()).isEqualTo("T24_CONNECTION_ERROR");
        assertThat(ex.getCause()).isInstanceOf(java.net.ConnectException.class);
    }

    @Test
    void adapterUnavailableExceptionShouldIncludeAdapterName() {
        var ex = new AdapterUnavailableException("ofs");
        assertThat(ex.getMessage()).contains("ofs");
        assertThat(ex.errorCode()).isEqualTo("ADAPTER_UNAVAILABLE");
    }
}
