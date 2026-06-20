package com.faisalaffan.javara.app.grpc;

import com.faisalaffan.javara.core.domain.model.T24FundTransfer;
import com.faisalaffan.javara.core.domain.model.T24MultiCommit;
import com.faisalaffan.javara.core.domain.model.T24TellerTransaction;
import com.faisalaffan.javara.core.domain.model.TransactionStatus;
import com.faisalaffan.javara.core.domain.vo.AccountNumber;
import com.faisalaffan.javara.core.domain.vo.Amount;
import com.faisalaffan.javara.core.domain.vo.IdempotencyKey;
import com.faisalaffan.javara.core.domain.vo.T24Reference;
import com.faisalaffan.javara.core.port.TransactionPort;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import com.google.protobuf.Timestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@GrpcService
public class TransactionGrpcService extends TransactionServiceGrpc.TransactionServiceImplBase {

    private final TransactionPort transactionPort;

    public TransactionGrpcService(TransactionPort transactionPort) {
        this.transactionPort = transactionPort;
    }

    @Override
    public void postFundTransfer(FundTransferRequest request, StreamObserver<TransactionResponse> responseObserver) {
        T24FundTransfer transfer = T24FundTransfer.builder()
            .transactionId(new IdempotencyKey(request.getIdempotencyKey()))
            .debitAccount(new AccountNumber(request.getDebitAccount()))
            .creditAccount(new AccountNumber(request.getCreditAccount()))
            .amount(new Amount(new BigDecimal(request.getAmount()), request.getCurrency()))
            .paymentDetails(request.getPaymentDetails())
            .channel(request.getChannel())
            .valueDate(request.getValueDate())
            .processingPriority(request.getProcessingPriority())
            .chargeCode(request.getChargeCode())
            .build();

        T24FundTransfer result = transactionPort.postFundTransfer(transfer);
        responseObserver.onNext(toGrpcResponse(result));
        responseObserver.onCompleted();
    }

    @Override
    public void postTellerTransaction(TellerRequest request, StreamObserver<TransactionResponse> responseObserver) {
        T24TellerTransaction tellerTx = T24TellerTransaction.builder()
            .transactionId(new IdempotencyKey(request.getIdempotencyKey()))
            .accountNumber(new AccountNumber(request.getAccountNumber()))
            .amount(new Amount(new BigDecimal(request.getAmount()), request.getCurrency()))
            .transactionCode(request.getTransactionCode())
            .tellerId(request.getTellerId())
            .branchCode(request.getBranchCode())
            .narration(request.getNarration())
            .build();

        T24TellerTransaction result = transactionPort.postTellerTransaction(tellerTx);
        responseObserver.onNext(toGrpcResponse(result));
        responseObserver.onCompleted();
    }

    @Override
    public void postMultiCommit(MultiCommitRequest request, StreamObserver<TransactionResponse> responseObserver) {
        List<T24FundTransfer> transfers = request.getTransactionsList().stream()
            .map(tx -> T24FundTransfer.builder()
                .transactionId(new IdempotencyKey(tx.getIdempotencyKey()))
                .debitAccount(new AccountNumber(tx.getDebitAccount()))
                .creditAccount(new AccountNumber(tx.getCreditAccount()))
                .amount(new Amount(new BigDecimal(tx.getAmount()), tx.getCurrency()))
                .paymentDetails(tx.getPaymentDetails())
                .channel(tx.getChannel())
                .valueDate(tx.getValueDate())
                .processingPriority(tx.getProcessingPriority())
                .chargeCode(tx.getChargeCode())
                .build())
            .toList();

        T24MultiCommit multiCommit = T24MultiCommit.builder()
            .batchId(request.getBatchId())
            .transactions(transfers)
            .commitMode(request.getCommitMode())
            .build();

        T24MultiCommit result = transactionPort.postMultiCommit(multiCommit);
        responseObserver.onNext(toGrpcResponse(result));
        responseObserver.onCompleted();
    }

    @Override
    public void getTransactionStatus(StatusRequest request, StreamObserver<StatusResponse> responseObserver) {
        TransactionStatus status = transactionPort.getTransactionStatus(new T24Reference(request.getT24Reference()));
        responseObserver.onNext(StatusResponse.newBuilder()
            .setT24Reference(request.getT24Reference())
            .setStatus(status.name())
            .setLastUpdated(Timestamp.newBuilder().setSeconds(Instant.now().getEpochSecond()).build())
            .build());
        responseObserver.onCompleted();
    }

    private TransactionResponse toGrpcResponse(T24FundTransfer result) {
        return TransactionResponse.newBuilder()
            .setTransactionId(result.transactionId() != null ? result.transactionId().value() : "")
            .setStatus(result.status() != null ? result.status().name() : "")
            .setT24Reference(result.t24Reference() != null ? result.t24Reference().value() : "")
            .setPostedAt(Timestamp.newBuilder().setSeconds(
                result.postedAt() != null ? result.postedAt().getEpochSecond() : 0).build())
            .setDebitAccount(result.debitAccount() != null ? result.debitAccount().value() : "")
            .setCreditAccount(result.creditAccount() != null ? result.creditAccount().value() : "")
            .setAmount(result.amount() != null ? result.amount().value().toPlainString() : "0")
            .setCurrency(result.amount() != null ? result.amount().currency() : "")
            .build();
    }

    private TransactionResponse toGrpcResponse(T24TellerTransaction result) {
        return TransactionResponse.newBuilder()
            .setTransactionId(result.transactionId() != null ? result.transactionId().value() : "")
            .setStatus("COMPLETED")
            .setT24Reference("")
            .setPostedAt(Timestamp.newBuilder().setSeconds(Instant.now().getEpochSecond()).build())
            .setDebitAccount(result.accountNumber() != null ? result.accountNumber().value() : "")
            .setCreditAccount("")
            .setAmount(result.amount() != null ? result.amount().value().toPlainString() : "0")
            .setCurrency(result.amount() != null ? result.amount().currency() : "")
            .build();
    }

    private TransactionResponse toGrpcResponse(T24MultiCommit result) {
        int txCount = result.transactions() != null ? result.transactions().size() : 0;
        return TransactionResponse.newBuilder()
            .setTransactionId(result.batchId() != null ? result.batchId() : "")
            .setStatus(txCount > 0 ? "COMPLETED" : "PENDING")
            .setT24Reference("")
            .setPostedAt(Timestamp.newBuilder().setSeconds(Instant.now().getEpochSecond()).build())
            .setDebitAccount("")
            .setCreditAccount("")
            .setAmount(String.valueOf(txCount))
            .setCurrency("")
            .build();
    }
}
