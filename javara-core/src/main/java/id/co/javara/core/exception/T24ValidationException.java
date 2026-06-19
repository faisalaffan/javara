package id.co.javara.core.exception;

public class T24ValidationException extends T24ResponseException {

    public T24ValidationException(String t24ErrorCode, String message) {
        super(t24ErrorCode, message);
    }
}
