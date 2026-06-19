package id.co.javara.iris;

import id.co.javara.core.domain.model.T24FundTransfer;
import org.springframework.context.ApplicationEvent;

/**
 * Spring {@link ApplicationEvent} published when an IRIS response is received from T24.
 * <p>
 * Carries the {@code correlationId} so the sender can match this response to its
 * original request, the deserialized {@link T24FundTransfer} result, a success indicator,
 * and an optional error message.
 */
public class IRISResponseEvent extends ApplicationEvent {

    private final String correlationId;
    private final T24FundTransfer result;
    private final boolean success;
    private final String errorMessage;

    /**
     * Creates a successful response event.
     *
     * @param source        the component that published the event (e.g. the listener)
     * @param correlationId request-reply correlation identifier
     * @param result        the deserialized fund transfer result from T24
     */
    public IRISResponseEvent(Object source, String correlationId, T24FundTransfer result) {
        super(source);
        this.correlationId = correlationId;
        this.result = result;
        this.success = true;
        this.errorMessage = null;
    }

    /**
     * Creates a failed response event.
     *
     * @param source        the component that published the event
     * @param correlationId request-reply correlation identifier
     * @param errorMessage  description of the failure
     */
    public IRISResponseEvent(Object source, String correlationId, String errorMessage) {
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

    /** The deserialized fund transfer result, or {@code null} when {@code success} is {@code false}. */
    public T24FundTransfer getResult() {
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
