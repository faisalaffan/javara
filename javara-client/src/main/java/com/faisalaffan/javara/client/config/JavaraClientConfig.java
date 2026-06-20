package com.faisalaffan.javara.client.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@AutoConfiguration
@EnableFeignClients(basePackages = "com.faisalaffan.javara.client")
@ComponentScan(basePackages = "com.faisalaffan.javara.client")
public class JavaraClientConfig {
}
