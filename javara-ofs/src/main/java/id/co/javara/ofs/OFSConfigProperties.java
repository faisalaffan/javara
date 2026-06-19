package id.co.javara.ofs;

import java.util.Base64;

public record OFSConfigProperties(
    String endpoint,
    String username,
    String password,
    String authType
) {
    public String authHeader() {
        if ("basic".equalsIgnoreCase(authType)) {
            return "Basic " + Base64.getEncoder()
                .encodeToString((username + ":" + password).getBytes());
        }
        return "";
    }
}
