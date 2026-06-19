package id.co.javara.iris;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import id.co.javara.core.domain.model.T24Customer;
import id.co.javara.core.domain.model.T24FundTransfer;
import id.co.javara.core.domain.model.T24MultiCommit;
import id.co.javara.core.domain.model.T24PaymentOrder;
import id.co.javara.core.domain.model.T24TellerTransaction;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Serializes domain entities to and from the IRIS canonical message format.
 * <p>
 * The canonical envelope contains:
 * <ul>
 *   <li>{@code messageId} — unique UUID for each message</li>
 *   <li>{@code correlationId} — request-reply correlation identifier</li>
 *   <li>{@code timestamp} — ISO-8601 instant of message creation</li>
 *   <li>{@code source} — origin system (always {@code "JAVARA"})</li>
 *   <li>{@code target} — destination system (always {@code "T24"})</li>
 *   <li>{@code messageType} — discriminator for the payload type</li>
 * </ul>
 * <p>
 * Supports JSON serialization. When {@code messageFormat} is {@code "XML"} and
 * jackson-dataformat-xml is not on the classpath, an {@link UnsupportedOperationException}
 * is thrown.
 */
public class IRISMessageSerializer {

    private final ObjectMapper objectMapper;
    private final String messageFormat;

    /**
     * Creates a serializer for the given format.
     *
     * @param messageFormat {@code "JSON"} (default) or {@code "XML"}
     */
    public IRISMessageSerializer(String messageFormat) {
        this.messageFormat = (messageFormat == null || messageFormat.isBlank()) ? "JSON" : messageFormat.toUpperCase();
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    }

    // ---------------------------------------------------------------
    // Serialization
    // ---------------------------------------------------------------

    /**
     * Serializes a fund transfer to the IRIS canonical format.
     *
     * @param transfer      the domain entity
     * @param correlationId correlation identifier for request-reply tracking
     * @return canonical JSON string
     */
    public String serializeFundTransfer(T24FundTransfer transfer, String correlationId) {
        IRISCanonicalMessage envelope = buildEnvelope(correlationId, "FundTransfer");
        return serialize(envelope, transfer);
    }

    /**
     * Serializes a customer to the IRIS canonical format.
     *
     * @param customer      the domain entity
     * @param correlationId correlation identifier for request-reply tracking
     * @return canonical JSON string
     */
    public String serializeCustomer(T24Customer customer, String correlationId) {
        IRISCanonicalMessage envelope = buildEnvelope(correlationId, "Customer");
        return serialize(envelope, customer);
    }

    /**
     * Serializes a teller transaction to the IRIS canonical format.
     *
     * @param tellerTx      the domain entity
     * @param correlationId correlation identifier for request-reply tracking
     * @return canonical JSON string
     */
    public String serializeTellerTransaction(T24TellerTransaction tellerTx, String correlationId) {
        IRISCanonicalMessage envelope = buildEnvelope(correlationId, "TellerTransaction");
        return serialize(envelope, tellerTx);
    }

    /**
     * Serializes a payment order to the IRIS canonical format.
     *
     * @param paymentOrder  the domain entity
     * @param correlationId correlation identifier for request-reply tracking
     * @return canonical JSON string
     */
    public String serializePaymentOrder(T24PaymentOrder paymentOrder, String correlationId) {
        IRISCanonicalMessage envelope = buildEnvelope(correlationId, "PaymentOrder");
        return serialize(envelope, paymentOrder);
    }

    /**
     * Serializes a multi-commit batch to the IRIS canonical format.
     *
     * @param multiCommit   the domain entity
     * @param correlationId correlation identifier for request-reply tracking
     * @return canonical JSON string
     */
    public String serializeMultiCommit(T24MultiCommit multiCommit, String correlationId) {
        IRISCanonicalMessage envelope = buildEnvelope(correlationId, "MultiCommit");
        return serialize(envelope, multiCommit);
    }

    // ---------------------------------------------------------------
    // Deserialization
    // ---------------------------------------------------------------

    /**
     * Deserializes a canonical IRIS message into a fund transfer.
     *
     * @param payload canonical JSON string
     * @return the deserialized fund transfer
     */
    public T24FundTransfer deserializeFundTransfer(String payload) {
        return deserialize(payload, T24FundTransfer.class);
    }

    /**
     * Deserializes a canonical IRIS message into a customer.
     *
     * @param payload canonical JSON string
     * @return the deserialized customer
     */
    public T24Customer deserializeCustomer(String payload) {
        return deserialize(payload, T24Customer.class);
    }

    // ---------------------------------------------------------------
    // Internals
    // ---------------------------------------------------------------

    private IRISCanonicalMessage buildEnvelope(String correlationId, String messageType) {
        return new IRISCanonicalMessage(
                UUID.randomUUID().toString(),
                correlationId,
                Instant.now(),
                "JAVARA",
                "T24",
                messageType
        );
    }

    @SuppressWarnings("unchecked")
    private String serialize(IRISCanonicalMessage envelope, Object payload) {
        if ("XML".equals(messageFormat)) {
            throw new UnsupportedOperationException(
                    "XML serialization requires jackson-dataformat-xml on the classpath");
        }

        var wrapper = new IRISMessageWrapper(envelope, payload);

        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(wrapper);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to serialize IRIS message for type "
                    + envelope.messageType(), e);
        }
    }

    private <T> T deserialize(String payload, Class<T> payloadType) {
        if (payload == null || payload.isBlank()) {
            throw new IllegalArgumentException("IRIS message payload must not be blank");
        }

        try {
            var root = objectMapper.readTree(payload);
            var payloadNode = root.get("payload");
            if (payloadNode == null) {
                throw new IllegalArgumentException("IRIS message missing 'payload' field");
            }
            return objectMapper.treeToValue(payloadNode, payloadType);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to deserialize IRIS message to " + payloadType.getSimpleName(), e);
        }
    }

    /**
     * Extracts the correlationId from a raw IRIS canonical message without fully
     * deserializing the payload. Useful for routing responses before the payload type
     * is known.
     *
     * @param payload canonical JSON string
     * @return the correlationId from the envelope
     */
    public String extractCorrelationId(String payload) {
        if (payload == null || payload.isBlank()) {
            throw new IllegalArgumentException("IRIS message payload must not be blank");
        }
        try {
            var root = objectMapper.readTree(payload);
            var envelope = root.get("envelope");
            if (envelope == null) {
                throw new IllegalArgumentException("IRIS message missing 'envelope' field");
            }
            var cid = envelope.get("correlationId");
            if (cid == null) {
                throw new IllegalArgumentException("IRIS message envelope missing 'correlationId'");
            }
            return cid.asText();
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to parse IRIS message envelope", e);
        }
    }

    /**
     * Extracts the messageType from a raw IRIS canonical message.
     *
     * @param payload canonical JSON string
     * @return the messageType from the envelope
     */
    public String extractMessageType(String payload) {
        if (payload == null || payload.isBlank()) {
            throw new IllegalArgumentException("IRIS message payload must not be blank");
        }
        try {
            var root = objectMapper.readTree(payload);
            var envelope = root.get("envelope");
            if (envelope == null) {
                throw new IllegalArgumentException("IRIS message missing 'envelope' field");
            }
            var mt = envelope.get("messageType");
            if (mt == null) {
                throw new IllegalArgumentException("IRIS message envelope missing 'messageType'");
            }
            return mt.asText();
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to parse IRIS message envelope", e);
        }
    }

    // ---------------------------------------------------------------
    // Canonical format DTOs
    // ---------------------------------------------------------------

    /**
     * Envelope header of an IRIS canonical message.
     */
    public record IRISCanonicalMessage(
            @JsonProperty("messageId") String messageId,
            @JsonProperty("correlationId") String correlationId,
            @JsonProperty("timestamp") @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC") Instant timestamp,
            @JsonProperty("source") String source,
            @JsonProperty("target") String target,
            @JsonProperty("messageType") String messageType
    ) {}

    /**
     * Full IRIS canonical message wrapping envelope and typed payload.
     */
    public record IRISMessageWrapper(
            @JsonProperty("envelope") IRISCanonicalMessage envelope,
            @JsonProperty("payload") Object payload
    ) {}
}
