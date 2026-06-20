package com.faisalaffan.javara.core.exception;

public class T24ResponseException extends JavaraException {

    private final String t24ErrorCode;

    public T24ResponseException(String t24ErrorCode, String message) {
        super("T24_RESPONSE_ERROR", message);
        this.t24ErrorCode = t24ErrorCode;
    }

    public String t24ErrorCode() {
        return t24ErrorCode;
    }
}
