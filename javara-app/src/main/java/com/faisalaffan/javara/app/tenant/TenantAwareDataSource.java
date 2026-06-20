package com.faisalaffan.javara.app.tenant;

public class TenantAwareDataSource {

    public String resolveTenantId() {
        String tenantId = TenantContext.getTenantId();
        return tenantId != null ? tenantId : "default";
    }

    public String resolveCompanyCode() {
        // Look up T24 company from tenant config — for now returns tenant ID
        return resolveTenantId();
    }
}
