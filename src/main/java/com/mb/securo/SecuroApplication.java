package com.mb.securo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.mb.securo")
public class SecuroApplication {

    public static void main(String[] args) {
        SpringApplication.run(SecuroApplication.class, args);
    }

}
