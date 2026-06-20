package com.faisalaffan.javara.app.adapter;

import com.faisalaffan.javara.app.JavaraProperties;
import com.faisalaffan.javara.core.exception.AdapterUnavailableException;
import com.faisalaffan.javara.core.port.TransactionPort;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component
public class AdapterResolver {

    private final Map<String, TransactionPort> adapters;
    private final JavaraProperties properties;

    public AdapterResolver(Map<String, TransactionPort> adapters, JavaraProperties properties) {
        this.adapters = adapters;
        this.properties = properties;
    }

    public TransactionPort resolve(String adapterName) {
        String name = adapterName != null ? adapterName : properties.t24().defaultAdapter();
        TransactionPort adapter = adapters.get(name + "Adapter");
        if (adapter == null) {
            throw new AdapterUnavailableException(name);
        }
        return adapter;
    }
}
