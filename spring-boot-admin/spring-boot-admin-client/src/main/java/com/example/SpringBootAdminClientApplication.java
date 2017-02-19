package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@EnableDiscoveryClient
@SpringBootApplication
@RestController
public class SpringBootAdminClientApplication {

 public static void main(String[] args) {
  SpringApplication.run(SpringBootAdminClientApplication.class, args);
 }

 @RequestMapping("/hi")
 String hi() {
  return "Hi";
 }
}
