package com.faisalaffan.javara.core.port;

import com.faisalaffan.javara.core.domain.model.T24FundTransfer;
import com.faisalaffan.javara.core.domain.model.T24MultiCommit;
import com.faisalaffan.javara.core.domain.model.T24PaymentOrder;
import com.faisalaffan.javara.core.domain.model.T24TellerTransaction;
import com.faisalaffan.javara.core.domain.model.TransactionStatus;
import com.faisalaffan.javara.core.domain.vo.AccountNumber;
import com.faisalaffan.javara.core.domain.vo.T24Reference;

/**
 * Port for T24 transaction operations.
 * Each adapter (OFS, TAFJ, IRIS, JMS) implements this interface.
 */
public interface TransactionPort {

    /**
     * Post a fund transfer transaction to T24.
     *
     * @param transfer the fund transfer details
     * @return posted transfer with T24 reference and status
     * @throws com.faisalaffan.javara.core.exception.T24ConnectionException if T24 is unreachable
     * @throws com.faisalaffan.javara.core.exception.T24TimeoutException    if request times out
     */
    T24FundTransfer postFundTransfer(T24FundTransfer transfer);

    /**
     * Post a teller transaction to T24.
     *
     * @param tellerTx the teller transaction details
     * @return posted teller transaction with T24 reference and status
     * @throws com.faisalaffan.javara.core.exception.T24ConnectionException if T24 is unreachable
     * @throws com.faisalaffan.javara.core.exception.T24TimeoutException    if request times out
     */
    T24TellerTransaction postTellerTransaction(T24TellerTransaction tellerTx);

    /**
     * Post a payment order to T24.
     *
     * @param paymentOrder the payment order details
     * @return posted payment order with T24 reference and status
     * @throws com.faisalaffan.javara.core.exception.T24ConnectionException if T24 is unreachable
     * @throws com.faisalaffan.javara.core.exception.T24TimeoutException    if request times out
     */
    T24PaymentOrder postPaymentOrder(T24PaymentOrder paymentOrder);

    /**
     * Post a batch of fund transfers (multi-commit) to T24.
     *
     * @param multiCommit the batch of fund transfers
     * @return posted batch with T24 references and status
     * @throws com.faisalaffan.javara.core.exception.T24ConnectionException if T24 is unreachable
     * @throws com.faisalaffan.javara.core.exception.T24TimeoutException    if request times out
     */
    T24MultiCommit postMultiCommit(T24MultiCommit multiCommit);

    /**
     * Retrieve the current status of a previously submitted transaction.
     *
     * @param reference the T24 reference of the transaction
     * @return current transaction status
     * @throws com.faisalaffan.javara.core.exception.T24ConnectionException if T24 is unreachable
     * @throws com.faisalaffan.javara.core.exception.T24TimeoutException    if request times out
     */
    TransactionStatus getTransactionStatus(T24Reference reference);

    /**
     * Check if this adapter is healthy and reachable.
     *
     * @return true if T24 responds to health check
     */
    boolean isHealthy();
}
