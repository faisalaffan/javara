package id.co.javara.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import java.math.BigDecimal;
import java.time.Instant;

@FeignClient(name = "javara-t24", url = "${javara.client.base-url:http://localhost:8080}")
public interface T24FundTransferClient {

    @PostMapping("/api/v1/t24/transactions/fund-transfer")
    @CircuitBreaker(name = "t24-fund-transfer")
    @Retry(name = "t24-fund-transfer")
    FundTransferResponse postFundTransfer(
        @RequestHeader("X-Idempotency-Key") String idempotencyKey,
        @RequestHeader("X-Channel-Code") String channel,
        @RequestBody FundTransferRequest request);

    record FundTransferRequest(
        String debitAccount, String creditAccount,
        BigDecimal amount, String currency,
        String paymentDetails, String channel
    ) {}

    record FundTransferResponse(
        String transactionId, String status,
        String t24Reference, Instant postedAt
    ) {}
}
