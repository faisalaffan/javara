package com.faisalaffan.javara.core.exception;

public class AdapterUnavailableException extends JavaraException {

    public AdapterUnavailableException(String adapterName) {
        super("ADAPTER_UNAVAILABLE", "T24 adapter '" + adapterName + "' is not available");
    }
}
