package id.co.javara.core.exception;

public class JavaraException extends RuntimeException {

    private final String errorCode;

    public JavaraException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public JavaraException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String errorCode() {
        return errorCode;
    }
}
