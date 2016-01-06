package com.example.ssource;

import org.springframework.cloud.stream.messaging.Source;
import org.springframework.integration.annotation.InboundChannelAdapter;
import org.springframework.integration.annotation.MessageEndpoint;

@MessageEndpoint
public class GreetingSource {

    // todo in order to send streams of messages inject MessageChannel and publish message

    @InboundChannelAdapter(Source.OUTPUT)
    public String greet() {
        return "" + System.currentTimeMillis();
    }

}
