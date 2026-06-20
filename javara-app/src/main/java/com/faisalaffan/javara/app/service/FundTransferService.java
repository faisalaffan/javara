package com.faisalaffan.javara.app.service;

import com.faisalaffan.javara.app.dto.FundTransferRequest;
import com.faisalaffan.javara.app.dto.FundTransferResponse;
import com.faisalaffan.javara.core.domain.model.T24FundTransfer;
import com.faisalaffan.javara.core.domain.vo.AccountNumber;
import com.faisalaffan.javara.core.domain.vo.Amount;
import com.faisalaffan.javara.core.domain.vo.IdempotencyKey;
import com.faisalaffan.javara.core.port.TransactionPort;
import org.springframework.stereotype.Service;

@Service
public class FundTransferService {

    private final TransactionPort transactionPort;

    public FundTransferService(TransactionPort transactionPort) {
        this.transactionPort = transactionPort;
    }

    public FundTransferResponse execute(FundTransferRequest request, String idempotencyKey) {
        var transfer = T24FundTransfer.builder()
            .transactionId(new IdempotencyKey(idempotencyKey))
            .debitAccount(new AccountNumber(request.debitAccount()))
            .creditAccount(new AccountNumber(request.creditAccount()))
            .amount(new Amount(request.amount(), request.currency()))
            .paymentDetails(request.paymentDetails())
            .channel(request.channel())
            .valueDate(request.valueDate())
            .processingPriority(request.processingPriority())
            .chargeCode(request.chargeCode())
            .build();

        var result = transactionPort.postFundTransfer(transfer);

        return new FundTransferResponse(
            result.transactionId().value(),
            result.status().name(),
            result.t24Reference() != null ? result.t24Reference().value() : null,
            result.postedAt(),
            result.debitAccount().value(),
            result.creditAccount().value(),
            result.amount() != null ? result.amount().value() : null,
            result.amount() != null ? result.amount().currency() : null
        );
    }
}
