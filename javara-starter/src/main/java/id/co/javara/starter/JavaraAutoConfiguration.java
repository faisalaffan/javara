package id.co.javara.starter;

import id.co.javara.app.JavaraProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(JavaraProperties.class)
@ComponentScan(basePackages = "id.co.javara")
public class JavaraAutoConfiguration {
}
