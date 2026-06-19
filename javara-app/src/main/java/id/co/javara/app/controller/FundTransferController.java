package id.co.javara.app.controller;

import id.co.javara.app.dto.FundTransferRequest;
import id.co.javara.app.dto.FundTransferResponse;
import id.co.javara.app.service.FundTransferService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/t24/transactions")
public class FundTransferController {

    private final FundTransferService service;

    public FundTransferController(FundTransferService service) {
        this.service = service;
    }

    @PostMapping("/fund-transfer")
    @ResponseStatus(HttpStatus.CREATED)
    public FundTransferResponse postFundTransfer(
            @RequestHeader("X-Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody FundTransferRequest request) {
        return service.execute(request, idempotencyKey);
    }
}
