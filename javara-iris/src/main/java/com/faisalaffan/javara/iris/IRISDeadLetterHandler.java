package com.faisalaffan.javara.iris;

import jakarta.jms.JMSException;
import jakarta.jms.Queue;
import jakarta.jms.QueueBrowser;
import jakarta.jms.TextMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import java.util.Enumeration;

/**
 * Handles dead-letter queue (DLQ) operations for IRIS messages that could not be
 * delivered or processed after exhausting retry attempts.
 * <p>
 * Provides facilities to:
 * <ul>
 *   <li>Move a failed message to the DLQ with error metadata</li>
 *   <li>Re-publish a message from the DLQ back to the outbound queue for retry</li>
 *   <li>Query the current depth of the DLQ</li>
 * </ul>
 */
@Component
public class IRISDeadLetterHandler {

    private static final Logger log = LoggerFactory.getLogger(IRISDeadLetterHandler.class);

    private final JmsTemplate jmsTemplate;
    private final IRISConfigProperties config;

    @Autowired
    public IRISDeadLetterHandler(JmsTemplate jmsTemplate, IRISConfigProperties config) {
        this.jmsTemplate = jmsTemplate;
        this.config = config;
    }

    /**
     * Moves a failed message to the dead-letter queue, attaching error metadata
     * as JMS string properties.
     *
     * @param originalPayload the original IRIS canonical message that failed
     * @param error           description of the failure reason
     */
    public void handleDeadLetter(String originalPayload, String error) {
        log.error("Moving message to DLQ: error={} queue={}", error, config.deadLetterQueue());

        jmsTemplate.send(config.deadLetterQueue(), session -> {
            TextMessage msg = session.createTextMessage(originalPayload);
            msg.setStringProperty("JAVARA_ERROR", error);
            msg.setLongProperty("JAVARA_TIMESTAMP", System.currentTimeMillis());
            msg.setStringProperty("JAVARA_ORIGIN", "IRIS");
            return msg;
        });
    }

    /**
     * Re-publishes all messages currently on the dead-letter queue back to the
     * outbound queue for retry.
     * <p>
     * Messages are forwarded one-by-one; the DLQ is drained in FIFO order.
     * After forwarding, each message is removed from the DLQ.
     */
    public void retryDeadLetter() {
        log.info("Retrying all messages from DLQ: {}", config.deadLetterQueue());

        jmsTemplate.execute(session -> {
            Queue dlq = session.createQueue(config.deadLetterQueue());
            Queue outbound = session.createQueue(config.outboundQueue());

            try (QueueBrowser browser = session.createBrowser(dlq)) {
                Enumeration<?> messages = browser.getEnumeration();
                int count = 0;

                while (messages.hasMoreElements()) {
                    Object msg = messages.nextElement();
                    if (msg instanceof TextMessage textMessage) {
                        String payload = textMessage.getText();
                        var producer = session.createProducer(outbound);
                        var forwardMsg = session.createTextMessage(payload);
                        forwardMsg.setStringProperty("JAVARA_RETRY", "true");
                        producer.send(forwardMsg);
                        producer.close();
                        count++;
                    }
                }
                log.info("Re-published {} messages from DLQ to outbound queue", count);
            } catch (JMSException e) {
                log.error("Failed to browse/retry DLQ messages", e);
            }
            return null;
        });
    }

    /**
     * Re-publishes a specific message from the dead-letter queue to the outbound
     * queue, matched by the original message body content.
     * <p>
     * This is a best-effort scan; if multiple messages contain the same payload,
     * only the first match is retried.
     *
     * @param messageId the {@code messageId} from the IRIS canonical envelope to retry
     */
    public void retryDeadLetter(String messageId) {
        log.info("Retrying DLQ message with messageId={}", messageId);

        jmsTemplate.execute(session -> {
            Queue dlq = session.createQueue(config.deadLetterQueue());
            Queue outbound = session.createQueue(config.outboundQueue());

            try (QueueBrowser browser = session.createBrowser(dlq)) {
                Enumeration<?> messages = browser.getEnumeration();

                while (messages.hasMoreElements()) {
                    Object msg = messages.nextElement();
                    if (msg instanceof TextMessage textMessage) {
                        String payload = textMessage.getText();
                        if (payload.contains("\"messageId\":\"" + messageId + "\"")
                                || payload.contains("<messageId>" + messageId + "</messageId>")) {
                            var producer = session.createProducer(outbound);
                            var forwardMsg = session.createTextMessage(payload);
                            forwardMsg.setStringProperty("JAVARA_RETRY", "true");
                            forwardMsg.setStringProperty("JAVARA_RETRY_MESSAGE_ID", messageId);
                            producer.send(forwardMsg);
                            producer.close();
                            log.info("Retried DLQ message messageId={}", messageId);
                            break;
                        }
                    }
                }
            } catch (JMSException e) {
                log.error("Failed to retry DLQ message messageId={}", messageId, e);
            }
            return null;
        });
    }

    /**
     * Returns the approximate number of messages currently on the dead-letter queue.
     *
     * @return queue depth count
     */
    public int getDeadLetterCount() {
        return jmsTemplate.execute(session -> {
            Queue dlq = session.createQueue(config.deadLetterQueue());
            try (QueueBrowser browser = session.createBrowser(dlq)) {
                Enumeration<?> messages = browser.getEnumeration();
                int count = 0;
                while (messages.hasMoreElements()) {
                    messages.nextElement();
                    count++;
                }
                return count;
            } catch (JMSException e) {
                log.error("Failed to count DLQ messages", e);
                return -1;
            }
        });
    }
}
