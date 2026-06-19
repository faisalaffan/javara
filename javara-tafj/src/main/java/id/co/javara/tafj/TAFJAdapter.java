package id.co.javara.tafj;

import id.co.javara.core.domain.model.T24Account;
import id.co.javara.core.domain.model.T24Customer;
import id.co.javara.core.domain.model.T24FundTransfer;
import id.co.javara.core.domain.model.T24MultiCommit;
import id.co.javara.core.domain.model.T24PaymentOrder;
import id.co.javara.core.domain.model.T24TellerTransaction;
import id.co.javara.core.domain.model.TransactionStatus;
import id.co.javara.core.domain.vo.CustomerId;
import id.co.javara.core.domain.vo.T24Reference;
import id.co.javara.core.port.CustomerPort;
import id.co.javara.core.port.TransactionPort;
import id.co.javara.ofs.OFSMessageBuilder;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * TAFJ adapter implementing TransactionPort and CustomerPort.
 * <p>
 * Communicates with T24 Transact via TAFJ-exposed REST and SOAP web services.
 * Reuses OFSMessageBuilder from javara-ofs for OFS message construction,
 * then sends the payload through TAFJ REST/SOAP transport.
 */
public class TAFJAdapter implements TransactionPort, CustomerPort {

    private final TAFJRestClient restClient;
    private final TAFJSoapClient soapClient;
    private final TAFJResponseParser responseParser;
    private final OFSMessageBuilder ofsMessageBuilder;
    private final TAFJConfigProperties config;

    public TAFJAdapter(TAFJConfigProperties config) {
        this.config = config;
        this.restClient = new TAFJRestClient(config);
        this.soapClient = new TAFJSoapClient(config);
        this.responseParser = new TAFJResponseParser();
        this.ofsMessageBuilder = new OFSMessageBuilder();
    }

    // ========================================================================
    // TransactionPort
    // ========================================================================

    @Override
    public T24FundTransfer postFundTransfer(T24FundTransfer transfer) {
        var envelope = ofsMessageBuilder.buildFundTransfer(transfer);
        String raw = restClient.postFundTransfer(envelope.xml());
        return responseParser.parseFundTransferResponse(raw);
    }

    @Override
    public T24TellerTransaction postTellerTransaction(T24TellerTransaction tellerTx) {
        throw new UnsupportedOperationException("TAFJ teller transaction not yet implemented");
    }

    @Override
    public T24PaymentOrder postPaymentOrder(T24PaymentOrder paymentOrder) {
        String ofs = buildPaymentOrderOFS(paymentOrder);
        String raw = restClient.postFundTransfer(ofs);
        var ft = responseParser.parseFundTransferResponse(raw);
        return T24PaymentOrder.builder()
            .transactionId(ft.transactionId())
            .debitAccount(ft.debitAccount())
            .creditAccount(ft.creditAccount())
            .amount(ft.amount())
            .paymentDetails(ft.paymentDetails())
            .beneficiaryName(paymentOrder.beneficiaryName())
            .beneficiaryBank(paymentOrder.beneficiaryBank())
            .valueDate(paymentOrder.valueDate())
            .build();
    }

    @Override
    public T24MultiCommit postMultiCommit(T24MultiCommit multiCommit) {
        throw new UnsupportedOperationException("TAFJ multi-commit not yet implemented");
    }

    @Override
    public TransactionStatus getTransactionStatus(T24Reference reference) {
        String raw = restClient.getEnquiry("TRANSACTION.STATUS",
            Map.of("ID", reference.value()));
        String statusField = responseParser.extractField(raw, "TRANSACTION.STATUS");
        if (statusField.isEmpty()) {
            return TransactionStatus.PENDING;
        }
        try {
            return TransactionStatus.valueOf(statusField.toUpperCase());
        } catch (IllegalArgumentException e) {
            return TransactionStatus.PENDING;
        }
    }

    @Override
    public boolean isHealthy() {
        try {
            restClient.getEnquiry("HEALTH", Collections.emptyMap());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // ========================================================================
    // CustomerPort
    // ========================================================================

    @Override
    public T24Customer getCustomer(CustomerId customerId) {
        String raw = restClient.getEnquiry("CUSTOMER.READ",
            Map.of("CUSTOMER.ID", customerId.value()));
        return responseParser.parseCustomerResponse(raw);
    }

    @Override
    public List<T24Customer> searchCustomers(String searchTerm) {
        String raw = restClient.getEnquiry("CUSTOMER.SEARCH",
            Map.of("SEARCH.TERM", searchTerm));
        T24Customer customer = responseParser.parseCustomerResponse(raw);
        return customer.cifNumber() != null
            ? List.of(customer)
            : Collections.emptyList();
    }

    @Override
    public T24Customer createCustomer(T24Customer customer) {
        var envelope = ofsMessageBuilder.buildCustomer(customer);
        String raw = restClient.postCustomer(envelope.xml());
        return responseParser.parseCustomerResponse(raw);
    }

    @Override
    public T24Customer updateCustomer(CustomerId customerId, T24Customer customer) {
        var envelope = ofsMessageBuilder.buildCustomer(customer);
        String raw = restClient.postCustomer(envelope.xml());
        return responseParser.parseCustomerResponse(raw);
    }

    @Override
    public List<T24Account> getCustomerAccounts(CustomerId customerId) {
        throw new UnsupportedOperationException("TAFJ getCustomerAccounts not yet implemented");
    }

    @Override
    public T24Account openAccount(CustomerId customerId, T24Account account) {
        throw new UnsupportedOperationException("TAFJ openAccount not yet implemented");
    }

    // ========================================================================
    // Internal helpers
    // ========================================================================

    private String buildPaymentOrderOFS(T24PaymentOrder po) {
        return "PAYMENT.ORDER,REVERS/PROCESS,," +
            "DEBIT.ACCT.NO:1:1=" + po.debitAccount().value() + ",," +
            "DEBIT.CURRENCY:1:1=" + po.amount().currency() + ",," +
            "DEBIT.AMOUNT:1:1=" + po.amount().value().toPlainString() + ",," +
            "CREDIT.ACCT.NO:1:1=" + po.creditAccount().value() + ",," +
            "CREDIT.CURRENCY:1:1=" + po.amount().currency() + ",," +
            "BENEFICIARY:1:1=" + (po.beneficiaryName() != null ? po.beneficiaryName() : "") + ",," +
            "BENEFICIARY.BANK:1:1=" + (po.beneficiaryBank() != null ? po.beneficiaryBank() : "") + ",," +
            "PAYMENT.DETAILS:1:1=" + (po.paymentDetails() != null ? po.paymentDetails() : "");
    }
}
