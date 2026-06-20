package com.faisalaffan.javara.jms;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.faisalaffan.javara.core.domain.model.T24Customer;
import com.faisalaffan.javara.core.domain.model.T24FundTransfer;
import com.faisalaffan.javara.core.domain.model.TransactionStatus;
import com.faisalaffan.javara.core.domain.vo.AccountNumber;
import com.faisalaffan.javara.core.domain.vo.Amount;
import com.faisalaffan.javara.core.domain.vo.CustomerId;
import com.faisalaffan.javara.core.domain.vo.IdempotencyKey;
import com.faisalaffan.javara.core.domain.vo.T24Reference;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.Session;
import jakarta.jms.TextMessage;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Converts between JAVARA domain entities and T24 JMS message format.
 * <p>
 * T24 JMS messages use a key-value OFSML-inspired format over text messages.
 * Each message carries correlation metadata as JMS properties for request-reply
 * matching.
 */
public class T24MessageConverter implements org.springframework.jms.support.converter.MessageConverter {

    private static final String PROP_MESSAGE_TYPE = "MESSAGE_TYPE";
    private static final String PROP_CORRELATION_ID = "CORRELATION_ID";
    private static final String PROP_CHANNEL = "CHANNEL";

    private static final String TYPE_FUND_TRANSFER = "FUNDS.TRANSFER";
    private static final String TYPE_CUSTOMER = "CUSTOMER";
    private static final String TYPE_RESPONSE = "RESPONSE";

    private final ObjectMapper objectMapper;

    public T24MessageConverter() {
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public Message toMessage(Object obj, Session session) throws JMSException {
        if (obj instanceof T24FundTransfer ft) {
            return buildFundTransferMessage(ft, session);
        }
        if (obj instanceof T24Customer customer) {
            return buildCustomerMessage(customer, session);
        }
        throw new JMSException("Unsupported message type: " + obj.getClass().getName());
    }

    @Override
    public Object fromMessage(Message message) throws JMSException {
        if (message instanceof TextMessage tm) {
            String type = message.getStringProperty(PROP_MESSAGE_TYPE);
            String correlationId = message.getStringProperty(PROP_CORRELATION_ID);
            return parseResponse(type, correlationId, tm.getText());
        }
        throw new JMSException("Unsupported JMS message class: " + message.getClass().getName());
    }

    // -----------------------------------------------------------------------
    // Builders: domain -> JMS TextMessage
    // -----------------------------------------------------------------------

    private Message buildFundTransferMessage(T24FundTransfer ft, Session session) throws JMSException {
        TextMessage msg = session.createTextMessage();
        msg.setText(buildFundTransferPayload(ft));
        msg.setStringProperty(PROP_MESSAGE_TYPE, TYPE_FUND_TRANSFER);
        msg.setStringProperty(PROP_CORRELATION_ID, ft.transactionId().value());
        msg.setStringProperty(PROP_CHANNEL, ft.channel() != null ? ft.channel() : "JMS");
        return msg;
    }

    private Message buildCustomerMessage(T24Customer customer, Session session) throws JMSException {
        TextMessage msg = session.createTextMessage();
        msg.setText(buildCustomerPayload(customer));
        msg.setStringProperty(PROP_MESSAGE_TYPE, TYPE_CUSTOMER);
        msg.setStringProperty(PROP_CORRELATION_ID, customer.customerId().value());
        return msg;
    }

    // -----------------------------------------------------------------------
    // Payload serialization (OFSML-inspired format)
    // -----------------------------------------------------------------------

    /*
     * Produces a delimited key-value string in T24 OFSML convention:
     *
     * FUNDS.TRANSFER,REVERS/PROCESS,,
     * DEBIT.ACCT.NO:1:1=ACC001,,
     * DEBIT.CURRENCY:1:1=USD,,
     * DEBIT.AMOUNT:1:1=1000.00,,
     * CREDIT.ACCT.NO:1:1=ACC002,,
     * CREDIT.CURRENCY:1:1=USD,,
     * PAYMENT.DETAILS:1:1=Invoice#1234
     */
    String buildFundTransferPayload(T24FundTransfer ft) {
        StringBuilder sb = new StringBuilder();
        sb.append("FUNDS.TRANSFER,REVERS/PROCESS,,");
        appendField(sb, "DEBIT.ACCT.NO", ft.debitAccount().value());
        appendField(sb, "DEBIT.CURRENCY", ft.amount().currency());
        appendField(sb, "DEBIT.AMOUNT", ft.amount().value().toPlainString());
        appendField(sb, "CREDIT.ACCT.NO", ft.creditAccount().value());
        appendField(sb, "CREDIT.CURRENCY", ft.amount().currency());
        if (ft.paymentDetails() != null && !ft.paymentDetails().isBlank()) {
            appendField(sb, "PAYMENT.DETAILS", ft.paymentDetails());
        }
        if (ft.valueDate() != null) {
            appendField(sb, "VALUE.DATE", ft.valueDate());
        }
        return sb.toString();
    }

    /*
     * CUSTOMER,REVERS/PROCESS,,
     * SHORT.NAME:1:1=John Doe,,
     * ID.TYPE:1:1=PASSPORT,,
     * ID.NUMBER:1:1=A12345678
     */
    String buildCustomerPayload(T24Customer customer) {
        StringBuilder sb = new StringBuilder();
        sb.append("CUSTOMER,REVERS/PROCESS,,");
        appendField(sb, "SHORT.NAME", customer.fullName());
        if (customer.idType() != null) {
            appendField(sb, "ID.TYPE", customer.idType());
        }
        if (customer.idNumber() != null) {
            appendField(sb, "ID.NUMBER", customer.idNumber());
        }
        if (customer.cifNumber() != null) {
            appendField(sb, "CIF.NUMBER", customer.cifNumber());
        }
        return sb.toString();
    }

    private void appendField(StringBuilder sb, String fieldName, String value) {
        sb.append(fieldName).append(":1:1=").append(value).append(",,");
    }

    // -----------------------------------------------------------------------
    // Response parsing
    // -----------------------------------------------------------------------

    Object parseResponse(String type, String correlationId, String payload) throws JMSException {
        if (TYPE_FUND_TRANSFER.equals(type) || TYPE_RESPONSE.equals(type)) {
            return parseFundTransferResponse(correlationId, payload);
        }
        if (TYPE_CUSTOMER.equals(type)) {
            return parseCustomerResponse(correlationId, payload);
        }
        // Fallback: return raw payload as a generic response map
        try {
            Map<String, String> map = new LinkedHashMap<>();
            map.put("correlationId", correlationId);
            map.put("type", type);
            map.put("payload", payload);
            return map;
        } catch (Exception e) {
            throw new JMSException("Failed to parse response: " + e.getMessage());
        }
    }

    private T24FundTransfer parseFundTransferResponse(String correlationId, String payload) throws JMSException {
        try {
            String t24Ref = extractField(payload, "T24.REFERENCE");
            String statusStr = extractField(payload, "TRANSACTION.STATUS");
            String debitAcct = extractField(payload, "DEBIT.ACCT.NO");
            String creditAcct = extractField(payload, "CREDIT.ACCT.NO");
            String currency = extractField(payload, "DEBIT.CURRENCY");
            String amountStr = extractField(payload, "DEBIT.AMOUNT");

            TransactionStatus status;
            try {
                status = (statusStr != null && !statusStr.isBlank())
                    ? TransactionStatus.valueOf(statusStr)
                    : TransactionStatus.COMPLETED;
            } catch (IllegalArgumentException e) {
                status = TransactionStatus.COMPLETED;
            }

            return T24FundTransfer.builder()
                .transactionId(new IdempotencyKey(correlationId != null ? correlationId : IdempotencyKey.generate().value()))
                .debitAccount(new AccountNumber(debitAcct.isBlank() ? "0" : debitAcct))
                .creditAccount(new AccountNumber(creditAcct.isBlank() ? "0" : creditAcct))
                .amount(new Amount(
                    amountStr.isBlank() ? BigDecimal.ZERO : new BigDecimal(amountStr),
                    currency.isBlank() ? "USD" : currency
                ))
                .t24Reference(t24Ref.isBlank() ? null : new T24Reference(t24Ref))
                .status(status)
                .postedAt(Instant.now())
                .build();
        } catch (Exception e) {
            throw new JMSException("Failed to parse fund transfer response: " + e.getMessage());
        }
    }

    private T24Customer parseCustomerResponse(String correlationId, String payload) throws JMSException {
        try {
            String cifNumber = extractField(payload, "CIF.NUMBER");
            String fullName = extractField(payload, "SHORT.NAME");
            String idType = extractField(payload, "ID.TYPE");
            String idNumber = extractField(payload, "ID.NUMBER");

            return new T24Customer(
                new CustomerId(correlationId != null ? correlationId : "UNKNOWN"),
                cifNumber.isBlank() ? null : cifNumber,
                fullName.isBlank() ? "Unknown" : fullName,
                idType.isBlank() ? null : idType,
                idNumber.isBlank() ? null : idNumber,
                null, null, null, null, null
            );
        } catch (Exception e) {
            throw new JMSException("Failed to parse customer response: " + e.getMessage());
        }
    }

    // -----------------------------------------------------------------------
    // Field extraction helpers
    // -----------------------------------------------------------------------

    /**
     * Extracts a field value from an OFSML-style payload.
     * Format: {@code FIELD.NAME:1:1=VALUE,,}
     */
    String extractField(String payload, String fieldName) {
        if (payload == null || payload.isBlank()) {
            return "";
        }
        String marker = fieldName + ":1:1=";
        int start = payload.indexOf(marker);
        if (start == -1) {
            return "";
        }
        start += marker.length();
        int end = payload.indexOf(",", start);
        if (end == -1) {
            end = payload.length();
        }
        return payload.substring(start, end).trim();
    }
}
