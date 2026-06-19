package id.co.javara.iris;

import id.co.javara.core.domain.model.T24FundTransfer;
import id.co.javara.core.domain.model.T24MultiCommit;
import id.co.javara.core.domain.model.T24PaymentOrder;
import id.co.javara.core.domain.model.T24TellerTransaction;
import id.co.javara.core.domain.model.TransactionStatus;
import id.co.javara.core.domain.vo.T24Reference;
import id.co.javara.core.exception.T24ConnectionException;
import id.co.javara.core.exception.T24ResponseException;
import id.co.javara.core.exception.T24TimeoutException;
import id.co.javara.core.port.TransactionPort;
import jakarta.jms.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

/**
 * IRIS (Integration & Runtime Integration Services) adapter implementing {@link TransactionPort}.
 * <p>
 * Uses an asynchronous request-reply pattern over JMS:
 * <ol>
 *   <li>Serializes the domain entity into the IRIS canonical format</li>
 *   <li>Sends the message to the outbound queue (→ T24)</li>
 *   <li>Waits for a correlated {@link IRISResponseEvent} published by {@link IRISMessageListener}</li>
 *   <li>Completes the {@link CompletableFuture} and returns the result</li>
 * </ol>
 * <p>
 * Operations other than {@code postFundTransfer} are fire-and-forget (no reply correlation).
 */
@Component
public class IRISAdapter implements TransactionPort {

    private static final Logger log = LoggerFactory.getLogger(IRISAdapter.class);

    private final IRISConfigProperties config;
    private final JmsTemplate jmsTemplate;
    private final IRISMessageSerializer serializer;
    private final ConnectionFactory connectionFactory;

    private final ConcurrentMap<String, CompletableFuture<IRISResponseEvent>> correlationMap =
            new ConcurrentHashMap<>();

    @Autowired
    public IRISAdapter(IRISConfigProperties config,
                       JmsTemplate jmsTemplate,
                       IRISMessageSerializer serializer,
                       ConnectionFactory connectionFactory) {
        this.config = config;
        this.jmsTemplate = jmsTemplate;
        this.serializer = serializer;
        this.connectionFactory = connectionFactory;
    }

    // ---------------------------------------------------------------
    // TransactionPort — Request-Reply
    // ---------------------------------------------------------------

    @Override
    public T24FundTransfer postFundTransfer(T24FundTransfer transfer) {
        String correlationId = UUID.randomUUID().toString();
        var future = new CompletableFuture<IRISResponseEvent>();
        correlationMap.put(correlationId, future);

        try {
            String payload = serializer.serializeFundTransfer(transfer, correlationId);

            log.debug("Sending fund transfer correlationId={} debit={} credit={} amount={}",
                    correlationId, transfer.debitAccount(), transfer.creditAccount(), transfer.amount());

            jmsTemplate.send(config.outboundQueue(), session -> session.createTextMessage(payload));

            IRISResponseEvent event = future.get(config.retryDelayMs() * config.maxRetries(),
                    TimeUnit.MILLISECONDS);

            if (event.isSuccess() && event.getResult() != null) {
                return event.getResult();
            }
            throw new T24ResponseException("IRIS_ERROR",
                    event.getErrorMessage() != null
                    ? event.getErrorMessage() : "IRIS response indicates failure");
        } catch (java.util.concurrent.TimeoutException e) {
            throw new T24TimeoutException("IRIS response timed out for correlationId: " + correlationId, e);
        } catch (T24ResponseException e) {
            throw e;
        } catch (Exception e) {
            throw new T24ConnectionException("IRIS request failed for correlationId: " + correlationId, e);
        } finally {
            correlationMap.remove(correlationId);
        }
    }

    // ---------------------------------------------------------------
    // TransactionPort — Fire-and-Forget
    // ---------------------------------------------------------------

    @Override
    public T24TellerTransaction postTellerTransaction(T24TellerTransaction tellerTx) {
        String correlationId = UUID.randomUUID().toString();
        String payload = serializer.serializeTellerTransaction(tellerTx, correlationId);

        log.debug("Sending teller transaction correlationId={} tellerId={} amount={}",
                correlationId, tellerTx.tellerId(), tellerTx.amount());

        jmsTemplate.send(config.outboundQueue(), session -> session.createTextMessage(payload));
        return tellerTx;
    }

    @Override
    public T24PaymentOrder postPaymentOrder(T24PaymentOrder paymentOrder) {
        String correlationId = UUID.randomUUID().toString();
        String payload = serializer.serializePaymentOrder(paymentOrder, correlationId);

        log.debug("Sending payment order correlationId={} beneficiary={} amount={}",
                correlationId, paymentOrder.beneficiaryName(), paymentOrder.amount());

        jmsTemplate.send(config.outboundQueue(), session -> session.createTextMessage(payload));
        return paymentOrder;
    }

    @Override
    public T24MultiCommit postMultiCommit(T24MultiCommit multiCommit) {
        String correlationId = UUID.randomUUID().toString();
        String payload = serializer.serializeMultiCommit(multiCommit, correlationId);

        log.debug("Sending multi-commit correlationId={} batchId={} txCount={}",
                correlationId, multiCommit.batchId(), multiCommit.transactions().size());

        jmsTemplate.send(config.outboundQueue(), session -> session.createTextMessage(payload));
        return multiCommit;
    }

    // ---------------------------------------------------------------
    // TransactionPort — Status & Health
    // ---------------------------------------------------------------

    @Override
    public TransactionStatus getTransactionStatus(T24Reference reference) {
        throw new UnsupportedOperationException(
                "getTransactionStatus is not supported in async IRIS mode. " +
                        "Use the T24 enquiry service for status lookups.");
    }

    @Override
    public boolean isHealthy() {
        try (var connection = connectionFactory.createConnection()) {
            connection.start();
            return true;
        } catch (Exception e) {
            log.warn("IRIS health check failed: {}", e.getMessage());
            return false;
        }
    }

    // ---------------------------------------------------------------
    // Event listener — completes correlation futures
    // ---------------------------------------------------------------

    /**
     * Handles incoming {@link IRISResponseEvent} published by {@link IRISMessageListener}.
     * Matches the event's {@code correlationId} against pending futures and completes them.
     */
    @EventListener
    public void handleResponseEvent(IRISResponseEvent event) {
        CompletableFuture<IRISResponseEvent> future = correlationMap.get(event.getCorrelationId());
        if (future != null) {
            log.debug("Completing future for correlationId={} success={}", event.getCorrelationId(), event.isSuccess());
            future.complete(event);
        }
    }
}
