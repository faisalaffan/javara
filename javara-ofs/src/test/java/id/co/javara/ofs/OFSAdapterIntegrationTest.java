package id.co.javara.ofs;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import id.co.javara.core.domain.model.T24FundTransfer;
import id.co.javara.core.domain.model.TransactionStatus;
import id.co.javara.core.domain.vo.AccountNumber;
import id.co.javara.core.domain.vo.Amount;
import id.co.javara.core.domain.vo.IdempotencyKey;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.math.BigDecimal;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

class OFSAdapterIntegrationTest {

    @RegisterExtension
    static WireMockExtension wiremock = WireMockExtension.newInstance()
        .options(wireMockConfig().dynamicPort())
        .build();

    @Test
    void shouldPostFundTransferSuccessfully() {
        wiremock.stubFor(post(urlEqualTo("/"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "text/xml")
                .withBody("""
                    FUNDS.TRANSFER,REVERS/PROCESS,,
                    DEBIT.ACCT.NO:1:1=12345-678-1-USD,,
                    T24.REFERENCE:1:1=T24/260620/00123,,
                    TRANSACTION.STATUS:1:1=COMPLETED
                    """)));

        var config = new OFSConfigProperties(
            wiremock.baseUrl(),
            "test-user",
            "test-pass",
            "basic"
        );

        var adapter = new OFSAdapter(config);
        var transfer = T24FundTransfer.builder()
            .transactionId(new IdempotencyKey("test-123"))
            .debitAccount(new AccountNumber("12345-678-1-USD"))
            .creditAccount(new AccountNumber("98765-432-1-USD"))
            .amount(new Amount(new BigDecimal("1000.00"), "USD"))
            .build();

        var result = adapter.postFundTransfer(transfer);

        assertThat(result.status()).isEqualTo(TransactionStatus.COMPLETED);
        assertThat(result.t24Reference().value()).isEqualTo("T24/260620/00123");
    }
}
