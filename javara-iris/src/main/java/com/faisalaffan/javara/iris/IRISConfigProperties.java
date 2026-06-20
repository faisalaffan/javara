package com.faisalaffan.javara.iris;

/**
 * Configuration properties for the IRIS (Integration & Runtime Integration Services) adapter.
 *
 * @param brokerUrl       ActiveMQ/Artemis broker URL, e.g. {@code tcp://iris-broker:61616}
 * @param username        JMS broker username
 * @param password        JMS broker password
 * @param inboundQueue    Queue for responses from T24 (IRIS consumer), e.g. {@code IRIS.INBOUND.T24}
 * @param outboundQueue   Queue for requests to T24 (IRIS producer), e.g. {@code IRIS.OUTBOUND.T24}
 * @param deadLetterQueue Queue for undeliverable messages, e.g. {@code IRIS.DLQ}
 * @param maxRetries      Maximum delivery retry attempts before dead-lettering (default {@code 3})
 * @param retryDelayMs    Delay in milliseconds between retries (default {@code 5000})
 * @param messageFormat   Serialization format — {@code "JSON"} (default) or {@code "XML"}
 */
public record IRISConfigProperties(
        String brokerUrl,
        String username,
        String password,
        String inboundQueue,
        String outboundQueue,
        String deadLetterQueue,
        int maxRetries,
        long retryDelayMs,
        String messageFormat
) {

    public IRISConfigProperties {
        if (brokerUrl == null || brokerUrl.isBlank()) {
            throw new IllegalArgumentException("brokerUrl must not be blank");
        }
        if (inboundQueue == null || inboundQueue.isBlank()) {
            throw new IllegalArgumentException("inboundQueue must not be blank");
        }
        if (outboundQueue == null || outboundQueue.isBlank()) {
            throw new IllegalArgumentException("outboundQueue must not be blank");
        }
        if (deadLetterQueue == null || deadLetterQueue.isBlank()) {
            deadLetterQueue = "IRIS.DLQ";
        }
        if (maxRetries <= 0) {
            maxRetries = 3;
        }
        if (retryDelayMs <= 0) {
            retryDelayMs = 5000;
        }
        if (messageFormat == null || messageFormat.isBlank()) {
            messageFormat = "JSON";
        }
    }
}
