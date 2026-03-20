package com.cattle;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.ComponentScan;


@SpringBootApplication
@ComponentScan(basePackages = "com.cattle")
@ConfigurationPropertiesScan("com.cattle.rules")
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}