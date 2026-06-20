package com.faisalaffan.javara.app.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record FundTransferResponse(
    String transactionId,
    String status,
    String t24Reference,
    Instant postedAt,
    String debitAccount,
    String creditAccount,
    BigDecimal amount,
    String currency
) {}
