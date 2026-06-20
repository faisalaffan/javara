package com.faisalaffan.javara.jms;

import jakarta.jms.ConnectionFactory;
import jakarta.jms.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.listener.DefaultMessageListenerContainer;

/**
 * Spring configuration for asynchronous JMS message listeners.
 * <p>
 * Provides a pre-configured {@link DefaultJmsListenerContainerFactory} that
 * can be used with {@code @JmsListener} annotations to process incoming T24
 * messages asynchronously. This is optional — the primary {@link JMSAdapter}
 * uses synchronous send-and-receive.
 * <p>
 * The container factory uses:
 * <ul>
 *   <li>Concurrent consumers: 3–10 (scales based on load)</li>
 *   <li>Client acknowledgement (consumer explicitly acknowledges each message)</li>
 *   <li>Auto startup (listeners start when the application context is ready)</li>
 * </ul>
 */
@Configuration
public class JMSListenerConfig {

    private static final Logger log = LoggerFactory.getLogger(JMSListenerConfig.class);

    /**
     * Creates a listener container factory configured for T24 message processing.
     *
     * @param connectionFactory the JMS connection factory to use
     * @return configured container factory
     */
    @Bean
    public DefaultJmsListenerContainerFactory jmsListenerContainerFactory(
            ConnectionFactory connectionFactory) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();

        factory.setConnectionFactory(connectionFactory);
        factory.setConcurrency("3-10");
        factory.setSessionAcknowledgeMode(Session.CLIENT_ACKNOWLEDGE);
        factory.setErrorHandler(t ->
            log.error("Error processing JMS message in listener container", t));

        // Enable auto startup so listeners begin consuming when context is ready
        factory.setAutoStartup(true);

        // Use a simple client ID prefix for durable subscriptions (if needed)
        factory.setClientId("javara-jms-listener-");

        log.info("JMS listener container factory configured: concurrency=3-10, ackMode=CLIENT_ACKNOWLEDGE");

        return factory;
    }
}
