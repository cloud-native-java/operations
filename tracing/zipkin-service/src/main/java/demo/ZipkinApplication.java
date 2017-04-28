package demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.sleuth.zipkin.stream.EnableZipkinStreamServer;

// <1>
@EnableZipkinStreamServer
@SpringBootApplication
public class ZipkinApplication {

 public static void main(String[] args) {
  SpringApplication.run(ZipkinApplication.class, args);
 }
}
