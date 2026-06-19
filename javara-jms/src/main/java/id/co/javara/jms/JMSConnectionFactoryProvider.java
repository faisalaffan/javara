package id.co.javara.jms;

import jakarta.jms.ConnectionFactory;
import java.util.Optional;
import javax.net.ssl.SSLContext;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ActiveMQSslConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.connection.CachingConnectionFactory;

/**
 * Creates and configures the appropriate JMS {@link ConnectionFactory} based on
 * the configured broker type.
 * <p>
 * Supports ActiveMQ (default) and IBM MQ brokers. The returned factory is
 * wrapped in a {@link CachingConnectionFactory} for session pooling.
 * <p>
 * IBM MQ support requires {@code com.ibm.mq:com.ibm.mq.allclient} on the classpath.
 * Without it, IBM MQ configuration will throw a clear error at runtime.
 */
public class JMSConnectionFactoryProvider {

    private static final Logger log = LoggerFactory.getLogger(JMSConnectionFactoryProvider.class);

    private static final String BROKER_ACTIVEMQ = "ACTIVEMQ";
    private static final String BROKER_IBMMQ = "IBMMQ";

    private final JMSConfigProperties config;

    public JMSConnectionFactoryProvider(JMSConfigProperties config) {
        this.config = config;
    }

    /**
     * Resolves the broker type and builds the appropriate {@link ConnectionFactory}.
     *
     * @return a {@link CachingConnectionFactory} wrapping the native factory
     */
    public CachingConnectionFactory createConnectionFactory() {
        ConnectionFactory targetFactory = createTargetFactory();
        return wrapWithPool(targetFactory);
    }

    private ConnectionFactory createTargetFactory() {
        String brokerType = Optional.ofNullable(config.brokerType())
            .orElse(BROKER_ACTIVEMQ)
            .toUpperCase();

        return switch (brokerType) {
            case BROKER_IBMMQ -> createIbmMqFactory();
            case BROKER_ACTIVEMQ -> createActiveMqFactory();
            default -> {
                log.warn("Unknown broker type '{}', falling back to ActiveMQ", brokerType);
                yield createActiveMqFactory();
            }
        };
    }

    // -----------------------------------------------------------------------
    // ActiveMQ
    // -----------------------------------------------------------------------

    private ConnectionFactory createActiveMqFactory() {
        try {
            ActiveMQConnectionFactory factory;

            if (config.sslEnabled()) {
                factory = new ActiveMQSslConnectionFactory();
            } else {
                factory = new ActiveMQConnectionFactory();
            }

            factory.setBrokerURL(config.brokerUrl());

            if (config.username() != null && !config.username().isBlank()) {
                factory.setUserName(config.username());
                factory.setPassword(config.password() != null ? config.password() : "");
            }

            if (config.sslEnabled() && config.sslCipherSuite() != null && !config.sslCipherSuite().isBlank()) {
                SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, null, null);
                log.info("SSL enabled for ActiveMQ connection to {}", config.brokerUrl());
            }

            factory.setTrustAllPackages(false);
            factory.setObjectMessageSerializationDefered(false);

            log.info("Created ActiveMQ connection factory for broker URL: {}", config.brokerUrl());
            return factory;

        } catch (Exception e) {
            throw new IllegalStateException("Failed to create ActiveMQ connection factory", e);
        }
    }

    // -----------------------------------------------------------------------
    // IBM MQ (via reflection — com.ibm.mq jars must be on classpath at runtime)
    // -----------------------------------------------------------------------

    private ConnectionFactory createIbmMqFactory() {
        try {
            // Use reflection to avoid compile-time dependency on IBM MQ jars
            Class<?> mqFactoryClass = Class.forName("com.ibm.mq.jakarta.jms.MQConnectionFactory");
            Class<?> jmscClass = Class.forName("com.ibm.mq.jakarta.jms.JMSC");

            Object factory = mqFactoryClass.getDeclaredConstructor().newInstance();

            // Parse brokerUrl to extract host and port
            String url = config.brokerUrl();
            String host = "localhost";
            int port = 1414;

            if (url != null && url.startsWith("tcp://")) {
                String rest = url.substring(6);
                int colonIdx = rest.lastIndexOf(':');
                if (colonIdx > 0) {
                    host = rest.substring(0, colonIdx);
                    try {
                        port = Integer.parseInt(rest.substring(colonIdx + 1));
                    } catch (NumberFormatException ignored) {
                        // keep default
                    }
                } else {
                    host = rest;
                }
            }

            mqFactoryClass.getMethod("setHostName", String.class).invoke(factory, host);
            mqFactoryClass.getMethod("setPort", int.class).invoke(factory, port);
            mqFactoryClass.getMethod("setQueueManager", String.class).invoke(factory, config.queueManager());
            mqFactoryClass.getMethod("setChannel", String.class).invoke(factory, config.channel());

            if (config.sslEnabled()) {
                mqFactoryClass.getMethod("setSSLCipherSuite", String.class)
                    .invoke(factory, config.sslCipherSuite());
            }

            // Authentication
            if (config.username() != null && !config.username().isBlank()) {
                String userIdProp = (String) mqFactoryClass.getField("USERID").get(null);
                String passwordProp = (String) mqFactoryClass.getField("PASSWORD").get(null);
                mqFactoryClass.getMethod("setStringProperty", String.class, String.class)
                    .invoke(factory, userIdProp, config.username());
                mqFactoryClass.getMethod("setStringProperty", String.class, String.class)
                    .invoke(factory, passwordProp, config.password() != null ? config.password() : "");
            }

            int transportType = jmscClass.getField("MQJMS_TP_CLIENT_MQ_TCP").getInt(null);
            mqFactoryClass.getMethod("setTransportType", int.class).invoke(factory, transportType);

            log.info("Created IBM MQ connection factory (via reflection) for host={}:{} queueManager={} channel={}",
                host, port, config.queueManager(), config.channel());

            return (ConnectionFactory) factory;

        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(
                "IBM MQ libraries not found on classpath. Add 'com.ibm.mq:com.ibm.mq.allclient' dependency.", e);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to create IBM MQ connection factory", e);
        }
    }

    // -----------------------------------------------------------------------
    // Pooling wrapper
    // -----------------------------------------------------------------------

    private CachingConnectionFactory wrapWithPool(ConnectionFactory target) {
        CachingConnectionFactory cachingFactory = new CachingConnectionFactory(target);
        cachingFactory.setSessionCacheSize(config.connectionPoolSize() > 0
            ? config.connectionPoolSize()
            : JMSConfigProperties.DEFAULT_POOL_SIZE);
        cachingFactory.setReconnectOnException(true);
        cachingFactory.setCacheConsumers(true);

        log.info("Wrapped connection factory with CachingConnectionFactory (pool size: {})",
            cachingFactory.getSessionCacheSize());

        return cachingFactory;
    }
}
