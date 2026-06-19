package id.co.javara.jms;

/**
 * Configuration properties for the JMS/MQ adapter.
 * <p>
 * Supports both ActiveMQ and IBM MQ broker types.
 * IBM-specific fields (queueManager, channel) are ignored when brokerType is ACTIVEMQ.
 *
 * @param brokerType         "ACTIVEMQ" or "IBMMQ"
 * @param brokerUrl          Broker URL (e.g. tcp://mq-server:61616 for ActiveMQ)
 * @param queueManager       IBM MQ queue manager name (IBMMQ only)
 * @param channel            IBM MQ server-connection channel (IBMMQ only)
 * @param username           Optional authentication username
 * @param password           Optional authentication password
 * @param inboundQueue       Queue name for receiving T24 responses (e.g. T24.RESPONSE.QUEUE)
 * @param outboundQueue      Queue name for sending T24 requests (e.g. T24.REQUEST.QUEUE)
 * @param connectionPoolSize Maximum pooled connections (default 10)
 * @param receiveTimeoutMs   Timeout in ms for synchronous receive (default 60000)
 * @param sslCipherSuite     Optional SSL/TLS cipher suite for secured connections
 * @param sslEnabled         Whether SSL/TLS is enabled (default false)
 */
public record JMSConfigProperties(
    String brokerType,
    String brokerUrl,
    String queueManager,
    String channel,
    String username,
    String password,
    String inboundQueue,
    String outboundQueue,
    int connectionPoolSize,
    int receiveTimeoutMs,
    String sslCipherSuite,
    boolean sslEnabled
) {

    /** Default connection pool size when none specified. */
    public static final int DEFAULT_POOL_SIZE = 10;

    /** Default receive timeout in milliseconds. */
    public static final int DEFAULT_RECEIVE_TIMEOUT_MS = 60000;

    public JMSConfigProperties {
        if (brokerType == null || brokerType.isBlank()) {
            brokerType = "ACTIVEMQ";
        }
        if (brokerUrl == null || brokerUrl.isBlank()) {
            brokerUrl = "tcp://localhost:61616";
        }
        if (connectionPoolSize <= 0) {
            connectionPoolSize = DEFAULT_POOL_SIZE;
        }
        if (receiveTimeoutMs <= 0) {
            receiveTimeoutMs = DEFAULT_RECEIVE_TIMEOUT_MS;
        }
    }
}
