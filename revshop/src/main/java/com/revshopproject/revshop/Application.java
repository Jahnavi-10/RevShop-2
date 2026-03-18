package com.revshopproject.revshop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.revshopproject.revshop") // Force scan of all sub-packages
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}