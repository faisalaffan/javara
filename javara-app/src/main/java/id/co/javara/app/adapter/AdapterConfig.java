package id.co.javara.app.adapter;

import id.co.javara.app.JavaraProperties;
import id.co.javara.core.port.CustomerPort;
import id.co.javara.core.port.TransactionPort;
import id.co.javara.ofs.OFSAdapter;
import id.co.javara.ofs.OFSConfigProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class AdapterConfig {

    @Bean
    @Primary
    public TransactionPort transactionPort(JavaraProperties properties) {
        return ofsAdapter(properties);
    }

    @Bean
    @Primary
    public CustomerPort customerPort(JavaraProperties properties) {
        return ofsAdapter(properties);
    }

    @Bean
    public OFSAdapter ofsAdapter(JavaraProperties properties) {
        var ofsConfig = new OFSConfigProperties(
            properties.t24().ofs().endpoint(),
            properties.t24().ofs().username(),
            properties.t24().ofs().password(),
            properties.t24().ofs().authType()
        );
        return new OFSAdapter(ofsConfig);
    }
}
