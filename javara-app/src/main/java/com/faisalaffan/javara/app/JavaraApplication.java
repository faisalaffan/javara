package com.faisalaffan.javara.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableScheduling
@EnableConfigurationProperties(JavaraProperties.class)
public class JavaraApplication {

    public static void main(String[] args) {
        SpringApplication.run(JavaraApplication.class, args);
    }
}
