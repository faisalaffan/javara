package com.faisalaffan.javara.starter;

import com.faisalaffan.javara.app.JavaraProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(JavaraProperties.class)
@ComponentScan(basePackages = "com.faisalaffan.javara")
public class JavaraAutoConfiguration {
}
