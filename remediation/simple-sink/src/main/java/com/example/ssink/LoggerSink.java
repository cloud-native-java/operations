package com.example.ssink;

import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;

@MessageEndpoint
public class LoggerSink {

	@ServiceActivator(inputChannel = Sink.INPUT)
	public void log(String message) {
		System.out.println(message);
	}
}
