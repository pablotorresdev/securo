package com.mb.conitrack;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.mb.conitrack")
public class ConitrackApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConitrackApplication.class, args);
    }

}
