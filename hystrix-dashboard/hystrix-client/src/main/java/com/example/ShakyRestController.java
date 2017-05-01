package com.example;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Random;

@RestController
class ShakyRestController {

 @Autowired
 private RestTemplate restTemplate;

 // <1>
 public ResponseEntity<String> fallback() {
  return ResponseEntity.ok("ONOES");
 }

 // <2>
 @HystrixCommand(fallbackMethod = "fallback")
 @RequestMapping(method = RequestMethod.GET, value = "/google")
 public ResponseEntity<String> google() {
  return this.proxy(URI.create("http://www.google.com/"));
 }

 @HystrixCommand(fallbackMethod = "fallback")
 @RequestMapping(method = RequestMethod.GET, value = "/yahoo")
 public ResponseEntity<String> yahoo() {
  return this.proxy(URI.create("http://www.yahoo.com"));
 }

 private ResponseEntity<String> proxy(URI url) {

  if (new Random().nextInt(100) > 50) {
   throw new RuntimeException("tripping circuit breaker!");
  }

  ResponseEntity<String> responseEntity = this.restTemplate.getForEntity(url,
   String.class);

  return ResponseEntity.ok()
   .contentType(responseEntity.getHeaders().getContentType())
   .body(responseEntity.getBody());
 }

}
