package id.co.javara.core.port;

import id.co.javara.core.domain.vo.AccountNumber;
import id.co.javara.core.domain.vo.T24Reference;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Port for T24 enquiry/read-only operations.
 * Each adapter (OFS, TAFJ, IRIS, JMS) implements this interface.
 */
public interface EnquiryPort {

    /**
     * Retrieve the current balance of an account.
     *
     * @param accountNumber the account to query
     * @return the current balance
     * @throws id.co.javara.core.exception.T24ConnectionException if T24 is unreachable
     * @throws id.co.javara.core.exception.T24TimeoutException    if request times out
     */
    BigDecimal getAccountBalance(AccountNumber accountNumber);

    /**
     * Retrieve full transaction details for a given T24 reference.
     *
     * @param reference the T24 reference of the transaction
     * @return map of field names to values containing transaction details
     * @throws id.co.javara.core.exception.T24ConnectionException if T24 is unreachable
     * @throws id.co.javara.core.exception.T24TimeoutException    if request times out
     */
    Map<String, Object> getTransactionDetails(T24Reference reference);

    /**
     * Check whether an account number is valid in T24.
     *
     * @param accountNumber the account to validate
     * @return true if the account exists and is active
     * @throws id.co.javara.core.exception.T24ConnectionException if T24 is unreachable
     * @throws id.co.javara.core.exception.T24TimeoutException    if request times out
     */
    boolean isAccountValid(AccountNumber accountNumber);
}
