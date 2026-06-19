package id.co.javara.ofs;

import id.co.javara.core.domain.model.T24Customer;
import id.co.javara.core.domain.model.T24FundTransfer;
import id.co.javara.core.domain.vo.AccountNumber;
import id.co.javara.core.domain.vo.Amount;
import id.co.javara.core.domain.vo.CustomerId;
import id.co.javara.core.domain.vo.IdempotencyKey;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

class OFSMessageBuilderTest {

    private final OFSMessageBuilder builder = new OFSMessageBuilder();

    @Test
    void shouldBuildFundTransferOFSMessage() {
        var transfer = T24FundTransfer.builder()
            .transactionId(new IdempotencyKey("uuid-123"))
            .debitAccount(new AccountNumber("12345-678-1-USD"))
            .creditAccount(new AccountNumber("98765-432-1-USD"))
            .amount(new Amount(new BigDecimal("250000.00"), "USD"))
            .paymentDetails("Test payment")
            .build();

        OFSEnvelope envelope = builder.buildFundTransfer(transfer);

        assertThat(envelope.xml())
            .contains("FUNDS.TRANSFER,REVERS/PROCESS")
            .contains("DEBIT.ACCT.NO")
            .contains("12345-678-1-USD")
            .contains("CREDIT.ACCT.NO")
            .contains("98765-432-1-USD")
            .contains("DEBIT.AMOUNT")
            .contains("250000.00")
            .contains("DEBIT.CURRENCY")
            .contains("USD")
            .contains("CREDIT.CURRENCY")
            .contains("Test payment");
    }

    @Test
    void shouldBuildCustomerCreationOFSMessage() {
        var customer = T24Customer.builder()
            .customerId(new CustomerId("CUST-001"))
            .cifNumber("CIF-1001")
            .fullName("John Doe")
            .idType("KTP")
            .idNumber("1234567890")
            .build();

        OFSEnvelope envelope = builder.buildCustomer(customer);

        assertThat(envelope.xml())
            .contains("CUSTOMER,REVERS/PROCESS")
            .contains("SHORT.NAME:1:1=John Doe");
    }

    @Test
    void shouldRejectNullFundTransfer() {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> builder.buildFundTransfer(null))
            .withMessage("Fund transfer must not be null");
    }

    @Test
    void shouldRejectNullCustomer() {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> builder.buildCustomer(null))
            .withMessage("Customer must not be null");
    }

    @Test
    void shouldBuildFundTransferWithEmptyPaymentDetails() {
        var transfer = T24FundTransfer.builder()
            .transactionId(new IdempotencyKey("uuid-456"))
            .debitAccount(new AccountNumber("11111-222-1-USD"))
            .creditAccount(new AccountNumber("33333-444-1-IDR"))
            .amount(new Amount(new BigDecimal("50000.00"), "USD"))
            .build();

        OFSEnvelope envelope = builder.buildFundTransfer(transfer);

        assertThat(envelope.xml())
            .contains("FUNDS.TRANSFER")
            .contains("11111-222-1-USD")
            .contains("33333-444-1-IDR")
            .contains("50000.00");
    }

    @Test
    void shouldBuildCustomerOFSWithFullDetails() {
        var customer = T24Customer.builder()
            .customerId(new CustomerId("CUST-002"))
            .cifNumber("CIF-2002")
            .fullName("Jane Smith")
            .idType("PASSPORT")
            .idNumber("AB123456")
            .branchCode("BR001")
            .status("ACTIVE")
            .address("123 Main St")
            .phone("+62123456789")
            .email("jane@example.com")
            .build();

        OFSEnvelope envelope = builder.buildCustomer(customer);

        assertThat(envelope.xml())
            .contains("CUSTOMER,REVERS/PROCESS")
            .contains("SHORT.NAME:1:1=Jane Smith");
    }
}
