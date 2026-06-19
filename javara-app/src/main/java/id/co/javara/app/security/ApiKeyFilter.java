package id.co.javara.app.security;

import id.co.javara.app.tenant.TenantContext;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 5)
public class ApiKeyFilter implements Filter {

    // In production, this comes from DB or vault
    private final Map<String, ApiKeyEntry> apiKeys = new ConcurrentHashMap<>();

    public ApiKeyFilter() {
        // Placeholder — real keys loaded from config/DB
        apiKeys.put(hash("javara-dev-key"), new ApiKeyEntry("dev-tenant", "INTERNET_BANKING", true));
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String apiKey = httpRequest.getHeader("X-API-Key");

        // Skip if no API key header or if OAuth2 is configured
        if (apiKey == null || apiKey.isBlank()) {
            chain.doFilter(request, response);
            return;
        }

        String keyHash = hash(apiKey);
        ApiKeyEntry entry = apiKeys.get(keyHash);

        if (entry == null || !entry.enabled) {
            httpResponse.setStatus(401);
            httpResponse.setContentType("application/json");
            httpResponse.getWriter().write(
                "{\"errorCode\":\"UNAUTHORIZED\",\"message\":\"Invalid or disabled API key\"}");
            return;
        }

        // Set tenant context from API key
        if (entry.tenantId != null) {
            TenantContext.setTenantId(entry.tenantId);
        }
        if (entry.defaultChannel != null) {
            TenantContext.setChannelCode(entry.defaultChannel);
        }

        chain.doFilter(request, response);
    }

    private String hash(String key) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(key.getBytes());
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    private record ApiKeyEntry(String tenantId, String defaultChannel, boolean enabled) {}
}
