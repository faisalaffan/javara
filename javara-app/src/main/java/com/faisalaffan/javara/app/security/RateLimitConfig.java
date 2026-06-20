package com.faisalaffan.javara.app.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import java.util.Map;

@ConfigurationProperties(prefix = "javara.t24.rate-limit")
public record RateLimitConfig(
    boolean enabled,
    Map<String, ChannelLimits> channels
) {
    public record ChannelLimits(
        int fundTransfer,
        int customerInquiry,
        int tellerTransaction,
        int paymentOrder
    ) {}

    public int getLimit(String channel, String operation) {
        if (!enabled || channels == null) return Integer.MAX_VALUE;
        ChannelLimits limits = channels.get(channel);
        if (limits == null) return Integer.MAX_VALUE;
        return switch (operation) {
            case "fund-transfer" -> limits.fundTransfer();
            case "customer-inquiry" -> limits.customerInquiry();
            case "teller-transaction" -> limits.tellerTransaction();
            case "payment-order" -> limits.paymentOrder();
            default -> Integer.MAX_VALUE;
        };
    }
}
