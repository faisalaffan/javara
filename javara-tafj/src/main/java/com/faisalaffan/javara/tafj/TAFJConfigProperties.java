package com.faisalaffan.javara.tafj;

import java.util.Base64;

public record TAFJConfigProperties(
    String restEndpoint,
    String soapEndpoint,
    String username,
    String password,
    String authType,
    String oauthTokenUrl,
    String oauthClientId,
    String oauthClientSecret
) {

    public String authHeader() {
        if ("basic".equalsIgnoreCase(authType)) {
            return "Basic " + Base64.getEncoder()
                .encodeToString((username + ":" + password).getBytes());
        }
        if ("oauth2".equalsIgnoreCase(authType)) {
            return "Bearer placeholder";
        }
        return "";
    }
}
