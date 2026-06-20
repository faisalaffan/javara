package com.faisalaffan.javara.app.grpc;

import com.faisalaffan.javara.core.domain.model.T24Account;
import com.faisalaffan.javara.core.domain.model.T24Customer;
import com.faisalaffan.javara.core.domain.vo.CustomerId;
import com.faisalaffan.javara.core.port.CustomerPort;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
public class CustomerGrpcService extends CustomerServiceGrpc.CustomerServiceImplBase {

    private final CustomerPort customerPort;

    public CustomerGrpcService(CustomerPort customerPort) {
        this.customerPort = customerPort;
    }

    @Override
    public void getCustomer(GetCustomerRequest request, StreamObserver<Customer> responseObserver) {
        T24Customer customer = customerPort.getCustomer(new CustomerId(request.getCustomerId()));
        Customer grpcCustomer = toGrpcCustomer(customer);
        responseObserver.onNext(grpcCustomer);
        responseObserver.onCompleted();
    }

    @Override
    public void searchCustomers(SearchCustomersRequest request, StreamObserver<SearchCustomersResponse> responseObserver) {
        var customers = customerPort.searchCustomers(request.getSearchTerm());
        var response = SearchCustomersResponse.newBuilder()
            .addAllCustomers(customers.stream().map(this::toGrpcCustomer).toList())
            .setTotalCount(customers.size())
            .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void createCustomer(CreateCustomerRequest request, StreamObserver<Customer> responseObserver) {
        T24Customer input = fromGrpcCreateRequest(request);
        T24Customer created = customerPort.createCustomer(input);
        responseObserver.onNext(toGrpcCustomer(created));
        responseObserver.onCompleted();
    }

    @Override
    public void updateCustomer(UpdateCustomerRequest request, StreamObserver<Customer> responseObserver) {
        T24Customer input = fromGrpcUpdateRequest(request);
        T24Customer updated = customerPort.updateCustomer(new CustomerId(request.getCustomerId()), input);
        responseObserver.onNext(toGrpcCustomer(updated));
        responseObserver.onCompleted();
    }

    @Override
    public void getCustomerAccounts(GetCustomerAccountsRequest request, StreamObserver<CustomerAccountsResponse> responseObserver) {
        var accounts = customerPort.getCustomerAccounts(new CustomerId(request.getCustomerId()));
        var response = CustomerAccountsResponse.newBuilder()
            .setCustomerId(request.getCustomerId())
            .addAllAccounts(accounts.stream().map(a -> Account.newBuilder()
                .setAccountNumber(a.accountNumber().value())
                .setCustomerId(a.customerId().value())
                .setCurrency(a.currency())
                .setAccountType(a.accountType() != null ? a.accountType() : "")
                .setBalance(a.balance() != null ? a.balance().toPlainString() : "0")
                .setStatus(a.status() != null ? a.status() : "")
                .setOpenedDate(a.openedDate() != null ? a.openedDate() : "")
                .build()).toList())
            .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private Customer toGrpcCustomer(T24Customer c) {
        return Customer.newBuilder()
            .setCustomerId(c.customerId() != null ? c.customerId().value() : "")
            .setCifNumber(c.cifNumber() != null ? c.cifNumber() : "")
            .setFullName(c.fullName() != null ? c.fullName() : "")
            .setIdType(c.idType() != null ? c.idType() : "")
            .setIdNumber(c.idNumber() != null ? c.idNumber() : "")
            .setBranchCode(c.branchCode() != null ? c.branchCode() : "")
            .setStatus(c.status() != null ? c.status() : "")
            .setAddress(c.address() != null ? c.address() : "")
            .setPhone(c.phone() != null ? c.phone() : "")
            .setEmail(c.email() != null ? c.email() : "")
            .build();
    }

    private T24Customer fromGrpcCreateRequest(CreateCustomerRequest r) {
        return T24Customer.builder()
            .customerId(new CustomerId(r.getCustomerId()))
            .cifNumber(r.getCifNumber())
            .fullName(r.getFullName())
            .idType(r.getIdType())
            .idNumber(r.getIdNumber())
            .branchCode(r.getBranchCode())
            .address(r.getAddress())
            .phone(r.getPhone())
            .email(r.getEmail())
            .build();
    }

    private T24Customer fromGrpcUpdateRequest(UpdateCustomerRequest r) {
        return T24Customer.builder()
            .customerId(new CustomerId(r.getCustomerId()))
            .cifNumber("")
            .fullName(r.getFullName())
            .idType(null)
            .idNumber(null)
            .branchCode(null)
            .address(r.getAddress())
            .phone(r.getPhone())
            .email(r.getEmail())
            .build();
    }
}
