package id.co.javara.client.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@AutoConfiguration
@EnableFeignClients(basePackages = "id.co.javara.client")
@ComponentScan(basePackages = "id.co.javara.client")
public class JavaraClientConfig {
}
