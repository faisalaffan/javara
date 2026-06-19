package id.co.javara.tafj;

import id.co.javara.core.exception.T24ConnectionException;
import id.co.javara.core.exception.T24TimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

public class TAFJRestClient {

    private final RestClient restClient;
    private final TAFJResponseParser responseParser;
    private final TAFJConfigProperties config;

    public TAFJRestClient(TAFJConfigProperties config) {
        this.config = config;
        this.responseParser = new TAFJResponseParser();
        this.restClient = RestClient.builder()
            .baseUrl(config.restEndpoint())
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML_VALUE)
            .defaultHeader(HttpHeaders.AUTHORIZATION, config.authHeader())
            .build();
    }

    /**
     * Post a fund transfer XML payload to the TAFJ REST endpoint.
     */
    public String postFundTransfer(String xmlPayload) {
        return doPost(config.restEndpoint() + "/FT", xmlPayload);
    }

    /**
     * Post a customer XML payload to the TAFJ REST endpoint.
     */
    public String postCustomer(String xmlPayload) {
        return doPost(config.restEndpoint() + "/CUSTOMER", xmlPayload);
    }

    /**
     * Execute an enquiry via the TAFJ REST endpoint.
     */
    public String getEnquiry(String enquiryName, Map<String, String> params) {
        try {
            URI uri = buildEnquiryUri(enquiryName, params);

            return restClient.get()
                .uri(uri)
                .retrieve()
                .body(String.class);
        } catch (ResourceAccessException e) {
            throw new T24TimeoutException("TAFJ enquiry request timed out", e);
        }
    }

    // ---- internal ----

    private String doPost(String url, String xmlPayload) {
        try {
            return restClient.post()
                .uri(new URI(url))
                .body(xmlPayload)
                .retrieve()
                .body(String.class);
        } catch (ResourceAccessException e) {
            throw new T24TimeoutException("TAFJ REST request timed out", e);
        } catch (URISyntaxException e) {
            throw new T24ConnectionException("Invalid TAFJ endpoint URI: " + url, e);
        }
    }

    private URI buildEnquiryUri(String enquiryName, Map<String, String> params) {
        UriComponentsBuilder builder = UriComponentsBuilder
            .fromUriString(config.restEndpoint())
            .path("/ENQUIRY")
            .pathSegment(enquiryName);

        if (params != null) {
            params.forEach(builder::queryParam);
        }

        return builder.build().toUri();
    }
}
