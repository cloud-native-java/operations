package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.sleuth.zipkin.stream.EnableZipkinStreamServer;

@EnableZipkinStreamServer
@SpringBootApplication
public class ZipkinQueryApplication {

	public static void main(String[] args) {
		SpringApplication.run(ZipkinQueryApplication.class, args);
	}
}
