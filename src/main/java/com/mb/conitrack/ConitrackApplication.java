package com.mb.conitrack;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.mb.conitrack")
@EnableScheduling
public class ConitrackApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConitrackApplication.class, args);
    }

}
