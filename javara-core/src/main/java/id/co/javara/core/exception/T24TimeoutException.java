package id.co.javara.core.exception;

public class T24TimeoutException extends JavaraException {

    public T24TimeoutException(String message) {
        super("T24_TIMEOUT", message);
    }

    public T24TimeoutException(String message, Throwable cause) {
        super("T24_TIMEOUT", message, cause);
    }
}
