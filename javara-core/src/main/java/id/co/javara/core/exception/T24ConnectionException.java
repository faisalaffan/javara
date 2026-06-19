package id.co.javara.core.exception;

public class T24ConnectionException extends JavaraException {

    public T24ConnectionException(String message, Throwable cause) {
        super("T24_CONNECTION_ERROR", message, cause);
    }
}
