package id.co.javara.core.domain.model;

import id.co.javara.core.domain.vo.CustomerId;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

class T24CustomerTest {

    @Test
    void shouldBuildCustomerWithAllFields() {
        var c = T24Customer.builder()
            .customerId(new CustomerId("CUST.001"))
            .cifNumber("CIF123456")
            .fullName("John Doe")
            .idType("KTP")
            .idNumber("3175123456789012")
            .branchCode("JKT001")
            .status("ACTIVE")
            .address("Jl. Sudirman No.1")
            .phone("+628123456789")
            .email("john@example.com")
            .build();

        assertThat(c.customerId().value()).isEqualTo("CUST.001");
        assertThat(c.cifNumber()).isEqualTo("CIF123456");
        assertThat(c.fullName()).isEqualTo("John Doe");
        assertThat(c.idType()).isEqualTo("KTP");
        assertThat(c.idNumber()).isEqualTo("3175123456789012");
        assertThat(c.branchCode()).isEqualTo("JKT001");
        assertThat(c.status()).isEqualTo("ACTIVE");
        assertThat(c.address()).isEqualTo("Jl. Sudirman No.1");
        assertThat(c.phone()).isEqualTo("+628123456789");
        assertThat(c.email()).isEqualTo("john@example.com");
    }

    @Test
    void shouldRejectMissingCustomerId() {
        assertThatIllegalStateException()
            .isThrownBy(() -> T24Customer.builder()
                .cifNumber("CIF123456")
                .fullName("John Doe")
                .build())
            .withMessageContaining("customerId");
    }

    @Test
    void shouldRejectMissingCifNumber() {
        assertThatIllegalStateException()
            .isThrownBy(() -> T24Customer.builder()
                .customerId(new CustomerId("CUST.001"))
                .fullName("John Doe")
                .build())
            .withMessageContaining("cifNumber");
    }

    @Test
    void shouldRejectMissingFullName() {
        assertThatIllegalStateException()
            .isThrownBy(() -> T24Customer.builder()
                .customerId(new CustomerId("CUST.001"))
                .cifNumber("CIF123456")
                .build())
            .withMessageContaining("fullName");
    }
}
