package com.faisalaffan.javara.app.tenant;

import org.springframework.boot.context.properties.ConfigurationProperties;
import java.util.List;

@ConfigurationProperties(prefix = "javara.t24.multi-tenancy")
public record TenantProperties(
    boolean enabled,
    String defaultTenant,
    List<TenantConfig> tenants
) {
    public record TenantConfig(
        String id,
        String t24Company,
        String ofsEndpoint,
        String tafjEndpoint,
        String description
    ) {}
}
