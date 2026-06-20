package com.faisalaffan.javara.iris;

import com.faisalaffan.javara.core.domain.model.T24Customer;
import org.springframework.context.ApplicationEvent;

/**
 * Spring {@link ApplicationEvent} published when an IRIS customer response is received from T24.
 * <p>
 * Carries the {@code correlationId} for request-reply matching, the deserialized
 * {@link T24Customer} result, a success indicator, and an optional error message.
 */
public class IRISCustomerResponseEvent extends ApplicationEvent {

    private final String correlationId;
    private final T24Customer result;
    private final boolean success;
    private final String errorMessage;

    /**
     * Creates a successful customer response event.
     */
    public IRISCustomerResponseEvent(Object source, String correlationId, T24Customer result) {
        super(source);
        this.correlationId = correlationId;
        this.result = result;
        this.success = true;
        this.errorMessage = null;
    }

    /**
     * Creates a failed customer response event.
     */
    public IRISCustomerResponseEvent(Object source, String correlationId, String errorMessage) {
        super(source);
        this.correlationId = correlationId;
        this.result = null;
        this.success = false;
        this.errorMessage = errorMessage;
    }

    /** The correlation identifier linking this response to its original request. */
    public String getCorrelationId() {
        return correlationId;
    }

    /** The deserialized customer result, or {@code null} when {@code success} is {@code false}. */
    public T24Customer getResult() {
        return result;
    }

    /** {@code true} if the IRIS response indicates success. */
    public boolean isSuccess() {
        return success;
    }

    /** Error description when {@code success} is {@code false}, or {@code null} otherwise. */
    public String getErrorMessage() {
        return errorMessage;
    }
}
