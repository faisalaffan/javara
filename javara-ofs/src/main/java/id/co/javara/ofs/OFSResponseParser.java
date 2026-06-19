package id.co.javara.ofs;

import id.co.javara.core.domain.model.T24FundTransfer;
import id.co.javara.core.domain.model.TransactionStatus;
import id.co.javara.core.domain.vo.AccountNumber;
import id.co.javara.core.domain.vo.Amount;
import id.co.javara.core.domain.vo.IdempotencyKey;
import id.co.javara.core.domain.vo.T24Reference;
import id.co.javara.core.exception.T24BusinessException;
import id.co.javara.core.exception.T24ResponseException;
import id.co.javara.core.exception.T24SystemException;
import java.time.Instant;

public class OFSResponseParser {

    public T24FundTransfer parseFundTransferResponse(String rawResponse) {
        if (rawResponse == null || rawResponse.isBlank()) {
            throw new T24ResponseException("EMPTY", "T24 response is null or empty");
        }

        if (rawResponse.contains("ERROR.CODE")) {
            String errorCode = extractField(rawResponse, "ERROR.CODE");
            String errorText = extractField(rawResponse, "ERROR.TEXT");
            throw new T24BusinessException(errorCode, errorText);
        }

        String t24Ref = extractField(rawResponse, "T24.REFERENCE");
        String status = extractField(rawResponse, "TRANSACTION.STATUS");

        if (t24Ref.isEmpty()) {
            throw new T24ResponseException("PARSE_ERROR", "T24.REFERENCE not found in response");
        }

        return T24FundTransfer.builder()
            .t24Reference(new T24Reference(t24Ref))
            .status(status.isEmpty() ? TransactionStatus.PENDING : TransactionStatus.valueOf(status))
            .postedAt(Instant.now())
            .debitAccount(new AccountNumber(extractField(rawResponse, "DEBIT.ACCT.NO")))
            .creditAccount(new AccountNumber("0"))
            .amount(new Amount(java.math.BigDecimal.ONE, "USD"))
            .transactionId(IdempotencyKey.generate())
            .build();
    }

    String extractField(String raw, String fieldName) {
        String marker = fieldName + ":1:1=";
        int start = raw.indexOf(marker);
        if (start == -1) return "";
        start += marker.length();
        int end = raw.indexOf(",", start);
        if (end == -1) end = raw.length();
        return raw.substring(start, end).trim();
    }
}
