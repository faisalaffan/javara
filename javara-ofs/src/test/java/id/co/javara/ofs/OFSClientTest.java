package id.co.javara.ofs;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import id.co.javara.core.domain.model.T24FundTransfer;
import id.co.javara.core.domain.model.TransactionStatus;
import id.co.javara.core.domain.vo.AccountNumber;
import id.co.javara.core.domain.vo.Amount;
import id.co.javara.core.domain.vo.IdempotencyKey;
import id.co.javara.core.exception.T24BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.math.BigDecimal;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OFSClientTest {

    @RegisterExtension
    static WireMockExtension wiremock = WireMockExtension.newInstance()
        .options(wireMockConfig().dynamicPort())
        .build();

    private OFSClient client;

    @BeforeEach
    void setUp() {
        var config = new OFSConfigProperties(
            wiremock.baseUrl(),
            "test-user",
            "test-pass",
            "basic"
        );
        client = new OFSClient(config);
    }

    @Test
    void shouldSendFundTransferAndParseResponse() {
        String t24Response = """
            FUNDS.TRANSFER,REVERS/PROCESS,,
            DEBIT.ACCT.NO:1:1=12345-678-1-USD,,
            T24.REFERENCE:1:1=T24/260620/00123,,
            TRANSACTION.STATUS:1:1=COMPLETED
            """;

        wiremock.stubFor(post(urlEqualTo("/"))
            .willReturn(aResponse()
                .withStatus(200)
                .withBody(t24Response)));

        var transfer = T24FundTransfer.builder()
            .transactionId(new IdempotencyKey("uuid-123"))
            .debitAccount(new AccountNumber("12345-678-1-USD"))
            .creditAccount(new AccountNumber("98765-432-1-USD"))
            .amount(new Amount(new BigDecimal("250000.00"), "USD"))
            .paymentDetails("Test payment")
            .build();

        T24FundTransfer result = client.postFundTransfer(transfer);

        assertThat(result.t24Reference().value()).isEqualTo("T24/260620/00123");
        assertThat(result.status()).isEqualTo(TransactionStatus.COMPLETED);
    }

    @Test
    void shouldThrowBusinessExceptionOnT24Error() {
        String errorResponse = """
            FUNDS.TRANSFER,REVERS/PROCESS,,
            ERROR.CODE:1:1=EB-AC.DORMANT,,
            ERROR.TEXT:1:1=Account is dormant
            """;

        wiremock.stubFor(post(urlEqualTo("/"))
            .willReturn(aResponse()
                .withStatus(200)
                .withBody(errorResponse)));

        var transfer = T24FundTransfer.builder()
            .transactionId(new IdempotencyKey("uuid-456"))
            .debitAccount(new AccountNumber("12345-678-1-USD"))
            .creditAccount(new AccountNumber("98765-432-1-USD"))
            .amount(new Amount(new BigDecimal("1000.00"), "USD"))
            .build();

        assertThatThrownBy(() -> client.postFundTransfer(transfer))
            .isInstanceOf(T24BusinessException.class)
            .hasMessageContaining("dormant");
    }
}
