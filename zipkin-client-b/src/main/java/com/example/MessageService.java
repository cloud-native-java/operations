package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.sleuth.Sampler;
import org.springframework.cloud.sleuth.sampler.AlwaysSampler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Map;

@RestController
@EnableAspectJAutoProxy(proxyTargetClass = true)
@SpringBootApplication
public class MessageService {

    @RequestMapping("/message")
    Map<String, String> getMessage() {
        return Collections.singletonMap("message",
                "Hi, " + System.currentTimeMillis());
    }

    @Bean
    Sampler<?> sampler() {
        return new AlwaysSampler();
    }

    public static void main(String[] args) {
        SpringApplication.run(MessageService.class, args);
    }
}
