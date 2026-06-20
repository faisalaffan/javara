package com.faisalaffan.javara.tafj;

import com.faisalaffan.javara.core.exception.T24ConnectionException;
import com.faisalaffan.javara.core.exception.T24TimeoutException;
import java.io.StringReader;
import java.io.StringWriter;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.client.WebServiceIOException;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.client.SoapFaultClientException;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;
import org.springframework.ws.transport.http.HttpUrlConnectionMessageSender;

public class TAFJSoapClient {

    private final WebServiceTemplate webServiceTemplate;
    private final TAFJConfigProperties config;

    public TAFJSoapClient(TAFJConfigProperties config) {
        this.config = config;

        SaajSoapMessageFactory messageFactory = new SaajSoapMessageFactory();
        messageFactory.afterPropertiesSet();

        this.webServiceTemplate = new WebServiceTemplate(messageFactory);
        this.webServiceTemplate.setDefaultUri(config.soapEndpoint());

        // Set auth header on each request
        this.webServiceTemplate.setMessageSender(new HttpUrlConnectionMessageSender() {
            @Override
            protected void prepareConnection(java.net.HttpURLConnection connection) {
                connection.setRequestProperty("Authorization", config.authHeader());
            }
        });
    }

    /**
     * Send a SOAP request to the TAFJ SOAP endpoint.
     *
     * @param soapAction the SOAP action header value (may be null or empty)
     * @param xmlPayload the XML body content to send inside the SOAP body
     * @return the response SOAP body content as a String
     * @throws T24TimeoutException      if the request times out
     * @throws T24ConnectionException   if a connection error occurs
     */
    public String sendSoapRequest(String soapAction, String xmlPayload) {
        try {
            return (String) webServiceTemplate.sendAndReceive(
                config.soapEndpoint(),
                (WebServiceMessage request) -> {
                    SoapMessage soapReq = (SoapMessage) request;
                    if (soapAction != null && !soapAction.isBlank()) {
                        soapReq.setSoapAction(soapAction);
                    }
                    // Write payload into the SOAP body
                    Source source = new StreamSource(new StringReader(xmlPayload));
                    Result result = soapReq.getPayloadResult();
                    TransformerFactory.newInstance().newTransformer().transform(source, result);
                },
                (WebServiceMessage response) -> {
                    try {
                        SoapMessage soapRes = (SoapMessage) response;
                        Source source = soapRes.getPayloadSource();
                        StringWriter writer = new StringWriter();
                        TransformerFactory.newInstance().newTransformer()
                            .transform(source, new StreamResult(writer));
                        return writer.toString();
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to read SOAP response", e);
                    }
                }
            );
        } catch (WebServiceIOException e) {
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("timeout")) {
                throw new T24TimeoutException("TAFJ SOAP request timed out", e);
            }
            throw new T24ConnectionException("TAFJ SOAP connection error", e);
        } catch (SoapFaultClientException e) {
            throw new T24ConnectionException("TAFJ SOAP fault: " + e.getMessage(), e);
        }
    }
}
