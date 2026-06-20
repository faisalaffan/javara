package com.faisalaffan.javara.core.port;

import com.faisalaffan.javara.core.domain.model.T24Account;
import com.faisalaffan.javara.core.domain.model.T24Customer;
import com.faisalaffan.javara.core.domain.vo.CustomerId;

import java.util.List;

/**
 * Port for T24 customer operations.
 * Each adapter (OFS, TAFJ, IRIS, JMS) implements this interface.
 */
public interface CustomerPort {

    /**
     * Retrieve a customer by their unique customer ID.
     *
     * @param customerId the customer's unique identifier
     * @return the customer details
     * @throws com.faisalaffan.javara.core.exception.T24ConnectionException if T24 is unreachable
     * @throws com.faisalaffan.javara.core.exception.T24TimeoutException    if request times out
     */
    T24Customer getCustomer(CustomerId customerId);

    /**
     * Search for customers matching the given search term.
     *
     * @param searchTerm the search term (name, ID, etc.)
     * @return list of matching customers
     * @throws com.faisalaffan.javara.core.exception.T24ConnectionException if T24 is unreachable
     * @throws com.faisalaffan.javara.core.exception.T24TimeoutException    if request times out
     */
    List<T24Customer> searchCustomers(String searchTerm);

    /**
     * Create a new customer in T24.
     *
     * @param customer the customer details to create
     * @return the created customer with assigned CIF
     * @throws com.faisalaffan.javara.core.exception.T24ConnectionException if T24 is unreachable
     * @throws com.faisalaffan.javara.core.exception.T24TimeoutException    if request times out
     */
    T24Customer createCustomer(T24Customer customer);

    /**
     * Update an existing customer in T24.
     *
     * @param customerId the customer's unique identifier
     * @param customer   the updated customer details
     * @return the updated customer
     * @throws com.faisalaffan.javara.core.exception.T24ConnectionException if T24 is unreachable
     * @throws com.faisalaffan.javara.core.exception.T24TimeoutException    if request times out
     */
    T24Customer updateCustomer(CustomerId customerId, T24Customer customer);

    /**
     * Retrieve all accounts belonging to a customer.
     *
     * @param customerId the customer's unique identifier
     * @return list of accounts owned by the customer
     * @throws com.faisalaffan.javara.core.exception.T24ConnectionException if T24 is unreachable
     * @throws com.faisalaffan.javara.core.exception.T24TimeoutException    if request times out
     */
    List<T24Account> getCustomerAccounts(CustomerId customerId);

    /**
     * Open a new account for a customer in T24.
     *
     * @param customerId the customer's unique identifier
     * @param account    the account details to open
     * @return the opened account
     * @throws com.faisalaffan.javara.core.exception.T24ConnectionException if T24 is unreachable
     * @throws com.faisalaffan.javara.core.exception.T24TimeoutException    if request times out
     */
    T24Account openAccount(CustomerId customerId, T24Account account);
}
