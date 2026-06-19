package id.co.javara.app.service;

import id.co.javara.app.dto.FundTransferRequest;
import id.co.javara.app.dto.FundTransferResponse;
import id.co.javara.core.domain.model.T24FundTransfer;
import id.co.javara.core.domain.vo.AccountNumber;
import id.co.javara.core.domain.vo.Amount;
import id.co.javara.core.domain.vo.IdempotencyKey;
import id.co.javara.core.port.TransactionPort;
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
