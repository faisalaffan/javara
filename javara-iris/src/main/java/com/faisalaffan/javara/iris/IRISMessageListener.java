package com.faisalaffan.javara.iris;

import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.TextMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

/**
 * Listens for inbound IRIS messages from T24 on the configured inbound queue.
 * <p>
 * Each incoming message is deserialized from the IRIS canonical format and
 * published as a Spring {@link org.springframework.context.ApplicationEvent}
 * so that the {@link IRISAdapter} (or any other consumer) can correlate and
 * complete the asynchronous request-reply cycle.
 */
@Component
public class IRISMessageListener {

    private static final Logger log = LoggerFactory.getLogger(IRISMessageListener.class);

    private final IRISMessageSerializer serializer;
    private final ApplicationEventPublisher eventPublisher;

    @Autowired
    public IRISMessageListener(IRISMessageSerializer serializer,
                               ApplicationEventPublisher eventPublisher) {
        this.serializer = serializer;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Handles all incoming messages on the IRIS inbound queue.
     * <p>
     * Inspects the {@code messageType} field in the canonical envelope to
     * determine the payload type and dispatches accordingly.
     *
     * @param jmsMessage the raw JMS message from the inbound queue
     */
    @JmsListener(destination = "${javara.t24.iris.inbound-queue}")
    public void onMessage(Message jmsMessage) {
        if (!(jmsMessage instanceof TextMessage textMessage)) {
            log.warn("Received non-text IRIS message, skipping: {}", jmsMessage);
            return;
        }

        try {
            String payload = textMessage.getText();
            String correlationId = serializer.extractCorrelationId(payload);
            String messageType = serializer.extractMessageType(payload);

            log.debug("Received IRIS message type={} correlationId={}", messageType, correlationId);

            switch (messageType) {
                case "FundTransfer" -> onFundTransferResponse(payload, correlationId);
                case "Customer"     -> onCustomerResponse(payload, correlationId);
                default -> log.warn("Unrecognized IRIS message type={}, correlationId={}",
                        messageType, correlationId);
            }
        } catch (JMSException e) {
            log.error("Failed to read JMS text message", e);
        } catch (Exception e) {
            log.error("Error processing IRIS inbound message", e);
        }
    }

    /**
     * Parses a fund transfer response from the canonical format and publishes
     * an {@link IRISResponseEvent}.
     */
    private void onFundTransferResponse(String payload, String correlationId) {
        try {
            var result = serializer.deserializeFundTransfer(payload);
            var event = new IRISResponseEvent(this, correlationId, result);
            eventPublisher.publishEvent(event);
            log.info("Published IRISResponseEvent correlationId={} success=true", correlationId);
        } catch (Exception e) {
            log.error("Failed to deserialize fund transfer response correlationId={}", correlationId, e);
            var event = new IRISResponseEvent(this, correlationId, e.getMessage());
            eventPublisher.publishEvent(event);
        }
    }

    /**
     * Parses a customer response from the canonical format and publishes
     * an {@link IRISCustomerResponseEvent}.
     */
    private void onCustomerResponse(String payload, String correlationId) {
        try {
            var result = serializer.deserializeCustomer(payload);
            var event = new IRISCustomerResponseEvent(this, correlationId, result);
            eventPublisher.publishEvent(event);
            log.info("Published IRISCustomerResponseEvent correlationId={} success=true", correlationId);
        } catch (Exception e) {
            log.error("Failed to deserialize customer response correlationId={}", correlationId, e);
            var event = new IRISCustomerResponseEvent(this, correlationId, e.getMessage());
            eventPublisher.publishEvent(event);
        }
    }
}
