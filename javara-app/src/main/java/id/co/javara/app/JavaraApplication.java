package id.co.javara.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(JavaraProperties.class)
public class JavaraApplication {

    public static void main(String[] args) {
        SpringApplication.run(JavaraApplication.class, args);
    }
}
