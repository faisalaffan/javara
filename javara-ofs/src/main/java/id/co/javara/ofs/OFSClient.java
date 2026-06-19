package id.co.javara.ofs;

import id.co.javara.core.domain.model.T24FundTransfer;
import id.co.javara.core.domain.model.T24Customer;
import id.co.javara.core.exception.T24ConnectionException;
import id.co.javara.core.exception.T24TimeoutException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

public class OFSClient {

    private final RestClient restClient;
    private final OFSMessageBuilder messageBuilder;
    private final OFSResponseParser responseParser;
    private final OFSConfigProperties config;

    public OFSClient(OFSConfigProperties config) {
        this.config = config;
        this.messageBuilder = new OFSMessageBuilder();
        this.responseParser = new OFSResponseParser();
        this.restClient = RestClient.builder()
            .baseUrl(config.endpoint())
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML_VALUE)
            .defaultHeader(HttpHeaders.AUTHORIZATION, config.authHeader())
            .build();
    }

    public T24FundTransfer postFundTransfer(T24FundTransfer transfer) {
        OFSEnvelope envelope = messageBuilder.buildFundTransfer(transfer);

        try {
            String response = restClient.post()
                .body(envelope.xml())
                .retrieve()
                .body(String.class);

            return responseParser.parseFundTransferResponse(response);
        } catch (ResourceAccessException e) {
            throw new T24TimeoutException("OFS request timed out", e);
        }
    }
}
