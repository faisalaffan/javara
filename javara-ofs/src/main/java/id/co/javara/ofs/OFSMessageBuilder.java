package id.co.javara.ofs;

import id.co.javara.core.domain.model.T24Customer;
import id.co.javara.core.domain.model.T24FundTransfer;

public class OFSMessageBuilder {

    private static final String OFS_TEMPLATE_FT =
        "FUNDS.TRANSFER,REVERS/PROCESS,," +
        "DEBIT.ACCT.NO:1:1=%s,," +
        "DEBIT.CURRENCY:1:1=%s,," +
        "DEBIT.AMOUNT:1:1=%s,," +
        "CREDIT.ACCT.NO:1:1=%s,," +
        "CREDIT.CURRENCY:1:1=%s,," +
        "PAYMENT.DETAILS:1:1=%s";

    public OFSEnvelope buildFundTransfer(T24FundTransfer transfer) {
        if (transfer == null) {
            throw new IllegalArgumentException("Fund transfer must not be null");
        }

        String xml = String.format(OFS_TEMPLATE_FT,
            transfer.debitAccount().value(),
            transfer.amount().currency(),
            transfer.amount().value().toPlainString(),
            transfer.creditAccount().value(),
            transfer.amount().currency(),
            transfer.paymentDetails() != null ? transfer.paymentDetails() : ""
        );

        return new OFSEnvelope(xml);
    }

    public OFSEnvelope buildCustomer(T24Customer customer) {
        if (customer == null) {
            throw new IllegalArgumentException("Customer must not be null");
        }

        String xml = "CUSTOMER,REVERS/PROCESS,," +
            "SHORT.NAME:1:1=" + customer.fullName();

        return new OFSEnvelope(xml);
    }
}
