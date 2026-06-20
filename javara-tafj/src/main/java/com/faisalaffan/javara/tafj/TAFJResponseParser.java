package com.faisalaffan.javara.tafj;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.faisalaffan.javara.core.domain.model.T24Customer;
import com.faisalaffan.javara.core.domain.model.T24FundTransfer;
import com.faisalaffan.javara.core.domain.model.TransactionStatus;
import com.faisalaffan.javara.core.domain.vo.AccountNumber;
import com.faisalaffan.javara.core.domain.vo.Amount;
import com.faisalaffan.javara.core.domain.vo.IdempotencyKey;
import com.faisalaffan.javara.core.domain.vo.T24Reference;
import com.faisalaffan.javara.core.exception.T24BusinessException;
import com.faisalaffan.javara.core.exception.T24ResponseException;
import com.faisalaffan.javara.core.exception.T24SystemException;
import java.math.BigDecimal;
import java.time.Instant;

public class TAFJResponseParser {

    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();
    private static final XmlMapper XML_MAPPER = new XmlMapper();

    public T24FundTransfer parseFundTransferResponse(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new T24ResponseException("EMPTY", "TAFJ response is null or empty");
        }

        String normalized = raw.trim();

        if (normalized.startsWith("{")) {
            return parseFundTransferJson(normalized);
        }
        return parseFundTransferOfs(normalized);
    }

    public T24Customer parseCustomerResponse(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new T24ResponseException("EMPTY", "TAFJ customer response is null or empty");
        }

        String normalized = raw.trim();

        if (normalized.startsWith("{")) {
            return parseCustomerJson(normalized);
        }
        return parseCustomerOfs(normalized);
    }

    /**
     * Extract a named field from an OFS-style response.
     * Supports both comma-separated OFS format and XML/JSON fallback.
     */
    public String extractField(String raw, String fieldName) {
        if (raw == null || raw.isBlank()) {
            return "";
        }

        if (raw.startsWith("{")) {
            return extractJsonField(raw, fieldName);
        }

        if (raw.startsWith("<")) {
            return extractXmlField(raw, fieldName);
        }

        // OFS-style: FIELD:1:1=value
        return extractOfsField(raw, fieldName);
    }

    // ---- OFS-style parsing ----

    private T24FundTransfer parseFundTransferOfs(String raw) {
        if (raw.contains("ERROR.CODE")) {
            String errorCode = extractOfsField(raw, "ERROR.CODE");
            String errorText = extractOfsField(raw, "ERROR.TEXT");
            throw new T24BusinessException(errorCode, errorText);
        }

        String systemMarker = extractOfsField(raw, "SYSTEM.ERROR");
        if (!systemMarker.isEmpty()) {
            throw new T24SystemException("SYS_ERR", systemMarker);
        }

        String t24Ref = extractOfsField(raw, "T24.REFERENCE");
        String status = extractOfsField(raw, "TRANSACTION.STATUS");

        if (t24Ref.isEmpty()) {
            throw new T24ResponseException("PARSE_ERROR", "T24.REFERENCE not found in TAFJ response");
        }

        return T24FundTransfer.builder()
            .t24Reference(new T24Reference(t24Ref))
            .status(status.isEmpty() ? TransactionStatus.PENDING : safeParseStatus(status))
            .postedAt(Instant.now())
            .debitAccount(new AccountNumber(extractOfsField(raw, "DEBIT.ACCT.NO")))
            .creditAccount(new AccountNumber(extractOfsField(raw, "CREDIT.ACCT.NO")))
            .amount(new Amount(parseAmount(extractOfsField(raw, "AMOUNT")), "USD"))
            .transactionId(IdempotencyKey.generate())
            .build();
    }

    private T24Customer parseCustomerOfs(String raw) {
        if (raw.contains("ERROR.CODE")) {
            String errorCode = extractOfsField(raw, "ERROR.CODE");
            String errorText = extractOfsField(raw, "ERROR.TEXT");
            throw new T24BusinessException(errorCode, errorText);
        }

        String cif = extractOfsField(raw, "CUSTOMER.CIF");
        String name = extractOfsField(raw, "SHORT.NAME");

        return T24Customer.builder()
            .cifNumber(cif)
            .fullName(name)
            .build();
    }

    private String extractOfsField(String raw, String fieldName) {
        String marker = fieldName + ":1:1=";
        int start = raw.indexOf(marker);
        if (start == -1) return "";
        start += marker.length();
        int end = raw.indexOf(",", start);
        if (end == -1) end = raw.length();
        return raw.substring(start, end).trim();
    }

    // ---- JSON parsing ----

    private T24FundTransfer parseFundTransferJson(String raw) {
        try {
            JsonNode root = JSON_MAPPER.readTree(raw);

            JsonNode error = root.get("errorCode");
            if (error != null && !error.asText().isEmpty()) {
                String code = error.asText();
                String message = root.path("errorMessage").asText("");
                if ("0".equals(code) || "SUCCESS".equalsIgnoreCase(code)) {
                    // success, continue
                } else {
                    throw new T24BusinessException(code, message);
                }
            }

            String t24Ref = root.path("t24Reference").asText("");
            if (t24Ref.isEmpty()) {
                t24Ref = root.path("transactionId").asText("");
            }

            if (t24Ref.isEmpty()) {
                throw new T24ResponseException("PARSE_ERROR", "T24 reference not found in JSON response");
            }

            String status = root.path("status").asText("");

            return T24FundTransfer.builder()
                .t24Reference(new T24Reference(t24Ref))
                .status(status.isEmpty() ? TransactionStatus.PENDING : safeParseStatus(status))
                .postedAt(Instant.now())
                .debitAccount(new AccountNumber(root.path("debitAccount").asText("")))
                .creditAccount(new AccountNumber(root.path("creditAccount").asText("")))
                .amount(new Amount(parseAmount(root.path("amount").asText("0")), "USD"))
                .transactionId(IdempotencyKey.generate())
                .build();
        } catch (JsonProcessingException e) {
            throw new T24ResponseException("PARSE_ERROR", "Failed to parse JSON response: " + e.getMessage());
        }
    }

    private T24Customer parseCustomerJson(String raw) {
        try {
            JsonNode root = JSON_MAPPER.readTree(raw);

            JsonNode error = root.get("errorCode");
            if (error != null && !error.asText().isEmpty()) {
                String code = error.asText();
                String message = root.path("errorMessage").asText("");
                if (!"0".equals(code) && !"SUCCESS".equalsIgnoreCase(code)) {
                    throw new T24BusinessException(code, message);
                }
            }

            String cif = root.path("cifNumber").asText("");
            String name = root.path("fullName").asText(root.path("shortName").asText(""));

            return T24Customer.builder()
                .cifNumber(cif)
                .fullName(name)
                .build();
        } catch (JsonProcessingException e) {
            throw new T24ResponseException("PARSE_ERROR", "Failed to parse customer JSON: " + e.getMessage());
        }
    }

    private String extractJsonField(String raw, String fieldName) {
        try {
            JsonNode root = JSON_MAPPER.readTree(raw);
            JsonNode node = root.get(fieldName);
            return node != null ? node.asText("") : "";
        } catch (JsonProcessingException e) {
            return "";
        }
    }

    // ---- XML parsing ----

    private String extractXmlField(String raw, String fieldName) {
        try {
            JsonNode root = XML_MAPPER.readTree(raw);
            JsonNode node = root.get(fieldName);
            if (node == null) {
                // try lowercase
                node = root.get(fieldName.toLowerCase());
            }
            return node != null ? node.asText("") : "";
        } catch (JsonProcessingException e) {
            return "";
        }
    }

    // ---- helpers ----

    private static TransactionStatus safeParseStatus(String s) {
        try {
            return TransactionStatus.valueOf(s.toUpperCase());
        } catch (IllegalArgumentException e) {
            return TransactionStatus.PENDING;
        }
    }

    private static BigDecimal parseAmount(String s) {
        if (s == null || s.isBlank()) return BigDecimal.ZERO;
        try {
            return new BigDecimal(s);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }
}
