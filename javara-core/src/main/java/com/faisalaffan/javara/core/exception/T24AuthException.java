package com.faisalaffan.javara.core.exception;

public class T24AuthException extends T24ConnectionException {

    public T24AuthException(String message) {
        super("Authentication failed: " + message, null);
    }
}
