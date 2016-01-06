package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.sleuth.Sampler;
import org.springframework.cloud.sleuth.sampler.AlwaysSampler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@EnableAspectJAutoProxy(proxyTargetClass = true)
@SpringBootApplication
public class MessageService {

    @Bean
    Sampler<?> sampler() {
        return new AlwaysSampler();
    }

    public static void main(String[] args) {
        SpringApplication.run(MessageService.class, args);
    }
}

@RestController
class MesssageServiceRestController {

    @RequestMapping("/")
    Map<String, String> message(HttpServletRequest httpRequest) {
        List<String> headers = Arrays.asList("x-span-id", "x-span-name", "x-trace-id");
        Map<String, String> response = new HashMap<>();
        response.put("message", "Hi, " + System.currentTimeMillis());
        headers
                .stream()
                .filter(h -> httpRequest.getHeader(h) != null)
                .forEach(h -> response.put(h, httpRequest.getHeader(h)));
        return response;
    }

}