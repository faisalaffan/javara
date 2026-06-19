package id.co.javara.app.tenant;

public class TenantContext {
    private static final ThreadLocal<String> TENANT_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> CHANNEL_CODE = new ThreadLocal<>();

    public static void setTenantId(String tenantId) { TENANT_ID.set(tenantId); }
    public static String getTenantId() { return TENANT_ID.get(); }
    public static void setChannelCode(String channelCode) { CHANNEL_CODE.set(channelCode); }
    public static String getChannelCode() { return CHANNEL_CODE.get(); }
    public static void clear() { TENANT_ID.remove(); CHANNEL_CODE.remove(); }
}
