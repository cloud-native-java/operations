package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

// <1>
@EnableDiscoveryClient
@EnableCircuitBreaker
@SpringBootApplication
public class HystrixCircuitBreakerApplication {

 public static void main(String[] args) {
  SpringApplication.run(HystrixCircuitBreakerApplication.class, args);
 }

 @Bean
 RestTemplate restTemplate() {
  return new RestTemplate();
 }
}
