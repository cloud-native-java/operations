package com.example.ssink;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.integration.annotation.IntegrationComponentScan;

@EnableBinding(Sink.class)
@IntegrationComponentScan
@SpringBootApplication
public class SimpleSinkApplication {

	public static void main(String[] args) {
		SpringApplication.run(SimpleSinkApplication.class, args);
	}
}

