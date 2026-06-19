package id.co.javara.ofs;

import id.co.javara.core.domain.model.*;
import id.co.javara.core.domain.vo.AccountNumber;
import id.co.javara.core.domain.vo.CustomerId;
import id.co.javara.core.domain.vo.T24Reference;
import id.co.javara.core.port.CustomerPort;
import id.co.javara.core.port.TransactionPort;
import java.util.List;
import java.util.Map;

public class OFSAdapter implements TransactionPort, CustomerPort {

    private final OFSClient client;
    private final OFSMessageBuilder messageBuilder;
    private final OFSResponseParser responseParser;

    public OFSAdapter(OFSConfigProperties config) {
        this.client = new OFSClient(config);
        this.messageBuilder = new OFSMessageBuilder();
        this.responseParser = new OFSResponseParser();
    }

    @Override
    public T24FundTransfer postFundTransfer(T24FundTransfer transfer) {
        return client.postFundTransfer(transfer);
    }

    @Override
    public T24TellerTransaction postTellerTransaction(T24TellerTransaction tellerTx) {
        // TODO: Implement teller transaction via OFS
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public T24PaymentOrder postPaymentOrder(T24PaymentOrder paymentOrder) {
        // TODO: Implement payment order via OFS
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public T24MultiCommit postMultiCommit(T24MultiCommit multiCommit) {
        // TODO: Implement multi-commit via OFS
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public TransactionStatus getTransactionStatus(T24Reference reference) {
        // TODO: Implement status inquiry via OFS enquiry
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public boolean isHealthy() {
        try {
            client.postFundTransfer(null); // health check
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // CustomerPort

    @Override
    public T24Customer getCustomer(CustomerId customerId) {
        // TODO: via OFS enquiry
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public List<T24Customer> searchCustomers(String searchTerm) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public T24Customer createCustomer(T24Customer customer) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public T24Customer updateCustomer(CustomerId customerId, T24Customer customer) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public List<T24Account> getCustomerAccounts(CustomerId customerId) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public T24Account openAccount(CustomerId customerId, T24Account account) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
