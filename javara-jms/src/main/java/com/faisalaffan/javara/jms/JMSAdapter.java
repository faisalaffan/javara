package com.faisalaffan.javara.jms;

import com.faisalaffan.javara.core.domain.model.T24Account;
import com.faisalaffan.javara.core.domain.model.T24Customer;
import com.faisalaffan.javara.core.domain.model.T24FundTransfer;
import com.faisalaffan.javara.core.domain.model.T24MultiCommit;
import com.faisalaffan.javara.core.domain.model.T24PaymentOrder;
import com.faisalaffan.javara.core.domain.model.T24TellerTransaction;
import com.faisalaffan.javara.core.domain.model.TransactionStatus;
import com.faisalaffan.javara.core.domain.vo.AccountNumber;
import com.faisalaffan.javara.core.domain.vo.CustomerId;
import com.faisalaffan.javara.core.domain.vo.T24Reference;
import com.faisalaffan.javara.core.exception.T24ConnectionException;
import com.faisalaffan.javara.core.exception.T24TimeoutException;
import com.faisalaffan.javara.core.port.CustomerPort;
import com.faisalaffan.javara.core.port.TransactionPort;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;

/**
 * JMS/MQ direct adapter that implements {@link TransactionPort} and
 * {@link CustomerPort} by sending T24 messages over JMS queues without
 * an IRIS middleware layer.
 * <p>
 * Messages are sent to the configured outbound queue and responses are
 * received synchronously from the inbound queue. Correlation is managed
 * via JMS {@code CORRELATION_ID} and {@code MESSAGE_TYPE} properties.
 */
public class JMSAdapter implements TransactionPort, CustomerPort {

    private static final Logger log = LoggerFactory.getLogger(JMSAdapter.class);

    private final JmsTemplate jmsTemplate;
    private final JMSConfigProperties config;
    private final T24MessageConverter converter;

    public JMSAdapter(JmsTemplate jmsTemplate, JMSConfigProperties config) {
        this.jmsTemplate = jmsTemplate;
        this.config = config;
        this.converter = new T24MessageConverter();
        this.jmsTemplate.setMessageConverter(this.converter);
        this.jmsTemplate.setReceiveTimeout(config.receiveTimeoutMs());
    }

    // -----------------------------------------------------------------------
    // TransactionPort
    // -----------------------------------------------------------------------

    @Override
    public T24FundTransfer postFundTransfer(T24FundTransfer transfer) {
        log.debug("Sending fund transfer {} to queue {}",
            transfer.transactionId().value(), config.outboundQueue());
        return sendAndReceive(transfer, config.outboundQueue(), config.inboundQueue());
    }

    @Override
    public T24TellerTransaction postTellerTransaction(T24TellerTransaction tellerTx) {
        // TODO: Implement teller transaction via JMS
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public T24PaymentOrder postPaymentOrder(T24PaymentOrder paymentOrder) {
        // TODO: Implement payment order via JMS
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public T24MultiCommit postMultiCommit(T24MultiCommit multiCommit) {
        // TODO: Implement multi-commit via JMS
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public TransactionStatus getTransactionStatus(T24Reference reference) {
        // TODO: Implement status inquiry via JMS
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public boolean isHealthy() {
        try {
            var connection = jmsTemplate.getConnectionFactory().createConnection();
            try {
                connection.start();
                log.debug("JMS health check passed");
                return true;
            } finally {
                connection.close();
            }
        } catch (Exception e) {
            log.warn("JMS health check failed: {}", e.getMessage());
            return false;
        }
    }

    // -----------------------------------------------------------------------
    // CustomerPort
    // -----------------------------------------------------------------------

    @Override
    public T24Customer getCustomer(CustomerId customerId) {
        log.debug("Fetching customer {} via JMS", customerId.value());

        var request = new T24Customer(
            customerId, null, "INQUIRY", null, null,
            null, null, null, null, null
        );
        try {
            Message response = jmsTemplate.sendAndReceive(
                config.outboundQueue(),
                session -> converter.toMessage(request, session)
            );
            if (response == null) {
                throw new T24TimeoutException(
                    "No response received for customer inquiry: " + customerId.value());
            }
            Object result = converter.fromMessage(response);
            if (result instanceof T24Customer c) {
                return c;
            }
            throw new T24ConnectionException(
                "Unexpected response type for customer inquiry: " + result.getClass().getName(), null);
        } catch (JMSException e) {
            throw new T24ConnectionException(
                "JMS customer inquiry failed: " + e.getMessage(), e);
        }
    }

    @Override
    public List<T24Customer> searchCustomers(String searchTerm) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public T24Customer createCustomer(T24Customer customer) {
        log.debug("Creating customer {} via JMS", customer.customerId().value());
        try {
            Message response = jmsTemplate.sendAndReceive(
                config.outboundQueue(),
                session -> converter.toMessage(customer, session)
            );
            if (response == null) {
                throw new T24TimeoutException(
                    "No response received for customer creation: " + customer.customerId().value());
            }
            Object result = converter.fromMessage(response);
            if (result instanceof T24Customer c) {
                return c;
            }
            throw new T24ConnectionException(
                "Unexpected response type for customer creation: " + result.getClass().getName(), null);
        } catch (JMSException e) {
            throw new T24ConnectionException(
                "JMS customer creation failed: " + e.getMessage(), e);
        }
    }

    @Override
    public T24Customer updateCustomer(CustomerId customerId, T24Customer customer) {
        var updated = new T24Customer(
            customerId,
            customer.cifNumber(),
            customer.fullName(),
            customer.idType(),
            customer.idNumber(),
            customer.branchCode(),
            customer.status(),
            customer.address(),
            customer.phone(),
            customer.email()
        );
        return createCustomer(updated);
    }

    @Override
    public List<T24Account> getCustomerAccounts(CustomerId customerId) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public T24Account openAccount(CustomerId customerId, T24Account account) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    // -----------------------------------------------------------------------
    // Internal helpers
    // -----------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    private <T> T sendAndReceive(T request, String outboundQueue, String inboundQueue) {
        try {
            Message response = jmsTemplate.sendAndReceive(
                outboundQueue,
                session -> converter.toMessage(request, session)
            );

            if (response == null) {
                throw new T24TimeoutException(
                    "No response received within " + config.receiveTimeoutMs()
                        + "ms for request on queue: " + outboundQueue);
            }

            String correlationId = response.getStringProperty("CORRELATION_ID");
            if (correlationId != null && request instanceof T24FundTransfer ft
                && !correlationId.equals(ft.transactionId().value())) {
                log.warn("Correlation ID mismatch: expected {} but received {}",
                    ft.transactionId().value(), correlationId);
            }

            Object result = converter.fromMessage(response);
            return (T) result;

        } catch (JMSException e) {
            throw new T24ConnectionException(
                "JMS send-and-receive failed on queue " + outboundQueue + ": " + e.getMessage(), e);
        }
    }
}
