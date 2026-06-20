package com.faisalaffan.javara.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record FundTransferRequest(
    @NotBlank String debitAccount,
    @NotBlank String creditAccount,
    @Positive BigDecimal amount,
    @NotBlank String currency,
    String paymentDetails,
    String channel,
    String valueDate,
    String processingPriority,
    String chargeCode
) {}
