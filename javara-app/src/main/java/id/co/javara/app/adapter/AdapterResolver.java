package id.co.javara.app.adapter;

import id.co.javara.app.JavaraProperties;
import id.co.javara.core.exception.AdapterUnavailableException;
import id.co.javara.core.port.TransactionPort;
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
