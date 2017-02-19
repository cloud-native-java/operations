package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@EnableDiscoveryClient
@RestController
@SpringBootApplication
public class MicroservicesDashboardClient2Application {

 public static void main(String[] args) {
  SpringApplication.run(MicroservicesDashboardClient2Application.class, args);
 }

 @RequestMapping("/client-2")
 String hi() {
  return "Client 2";
 }
}
