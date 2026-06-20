package com.faisalaffan.javara.core.exception;

public class T24BusinessException extends T24ResponseException {

    public T24BusinessException(String t24ErrorCode, String message) {
        super(t24ErrorCode, message);
    }
}
