package com.faisalaffan.javara.jms;

import jakarta.jms.ConnectionFactory;
import jakarta.jms.DeliveryMode;
import jakarta.jms.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;

/**
 * Creates a pre-configured {@link JmsTemplate} for T24 JMS communication.
 * <p>
 * Ensures persistent delivery, explicit QoS, and the T24 message converter
 * are wired into the template.
 */
public class JMSTemplateProvider {

    private static final Logger log = LoggerFactory.getLogger(JMSTemplateProvider.class);

    private final JMSConfigProperties config;
    private final T24MessageConverter converter;

    public JMSTemplateProvider(JMSConfigProperties config, T24MessageConverter converter) {
        this.config = config;
        this.converter = converter;
    }

    /**
     * Builds a fully configured {@link JmsTemplate} ready for synchronous
     * send-and-receive operations against T24 queues.
     *
     * @param connectionFactory the (preferably pooled) connection factory
     * @return configured JmsTemplate
     */
    public JmsTemplate createJmsTemplate(ConnectionFactory connectionFactory) {
        JmsTemplate template = new JmsTemplate(connectionFactory);

        // Use the T24 message converter for automatic domain object conversion
        template.setMessageConverter(converter);

        // Synchronous receive timeout for request-reply operations
        template.setReceiveTimeout(config.receiveTimeoutMs());

        // Ensure messages are persisted by the broker
        template.setExplicitQosEnabled(true);
        template.setDeliveryMode(DeliveryMode.PERSISTENT);

        // Session acknowledgement: auto-acknowledge (broker removes message after delivery)
        template.setSessionAcknowledgeMode(Session.AUTO_ACKNOWLEDGE);

        // Disable default destination — the adapter always specifies queue explicitly
        template.setDefaultDestinationName(null);

        log.info("JmsTemplate configured: receiveTimeout={}ms, deliveryMode=PERSISTENT",
            config.receiveTimeoutMs());

        return template;
    }
}
