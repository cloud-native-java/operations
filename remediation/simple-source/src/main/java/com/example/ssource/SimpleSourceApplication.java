package com.example.ssource;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.integration.annotation.IntegrationComponentScan;

@EnableBinding(Source.class)
@IntegrationComponentScan
@SpringBootApplication
public class SimpleSourceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SimpleSourceApplication.class, args);
    }
}

