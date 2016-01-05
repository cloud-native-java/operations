package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.sleuth.Sampler;
import org.springframework.cloud.sleuth.sampler.AlwaysSampler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
@EnableAspectJAutoProxy(proxyTargetClass = true)
@SpringBootApplication
public class MessageService {

    @RequestMapping("/")
    Map<String, String> getMessage(@RequestHeader(name = "x-trace-id", required = false) String traceId) {

        Map<String, String> response = new HashMap<>();
        response.put("message", "Hi, " + System.currentTimeMillis());
        if (StringUtils.hasText(traceId)) {
            response.put("trace", traceId);
        }
        return response;
    }

    @Bean
    Sampler<?> sampler() {
        return new AlwaysSampler();
    }

    public static void main(String[] args) {
        SpringApplication.run(MessageService.class, args);
    }
}
