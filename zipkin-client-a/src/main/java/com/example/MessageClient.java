package com.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.sleuth.Sampler;
import org.springframework.cloud.sleuth.sampler.AlwaysSampler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Map;

@RestController
@EnableAspectJAutoProxy(proxyTargetClass = true)
@SpringBootApplication
public class MessageClient {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${message-service}")
    private String host;

    @RequestMapping("/")
    String getMessageFromAnotherService() {

        ParameterizedTypeReference<Map<String, String>> reference =
                new ParameterizedTypeReference<Map<String, String>>() {
                };

        Map<String, String> msg = this.restTemplate.exchange(
                this.host + "/", HttpMethod.GET, null, reference).getBody();

        return msg.get("message");
    }

    @Bean
    Sampler<?> sampler() {
        return new AlwaysSampler();
    }

    public static void main(String[] args) {
        SpringApplication.run(MessageClient.class, args);
    }
}
