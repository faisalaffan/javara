package id.co.javara.ofs;

import id.co.javara.core.domain.model.T24FundTransfer;
import id.co.javara.core.domain.model.TransactionStatus;
import id.co.javara.core.exception.T24BusinessException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OFSResponseParserTest {

    private final OFSResponseParser parser = new OFSResponseParser();

    @Test
    void shouldParseSuccessResponse() {
        String t24Response = """
            FUNDS.TRANSFER,REVERS/PROCESS,,
            DEBIT.ACCT.NO:1:1=12345-678-1-USD,,
            T24.REFERENCE:1:1=T24/260620/00123,,
            TRANSACTION.STATUS:1:1=COMPLETED
            """;

        T24FundTransfer result = parser.parseFundTransferResponse(t24Response);

        assertThat(result.t24Reference().value()).isEqualTo("T24/260620/00123");
        assertThat(result.status()).isEqualTo(TransactionStatus.COMPLETED);
    }

    @Test
    void shouldParseErrorResponse() {
        String errorResponse = """
            FUNDS.TRANSFER,REVERS/PROCESS,,
            ERROR.CODE:1:1=EB-AC.DORMANT,,
            ERROR.TEXT:1:1=Account is dormant
            """;

        assertThatThrownBy(() -> parser.parseFundTransferResponse(errorResponse))
            .isInstanceOf(T24BusinessException.class)
            .hasMessageContaining("dormant");
    }
}
