package demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicBoolean;

@RestController
@EnableBinding(Source.class)
@SpringBootApplication
public class ProducerApplication {

 private final AtomicBoolean run = new AtomicBoolean(false);

 private final Source channels;

 public ProducerApplication(Source channels) {
  this.channels = channels;
 }

 @GetMapping("/start")
 public void start() {
  this.run.set(true);
 }

 @GetMapping("/stop")
 public void stop() {
  this.run.set(false);
 }

 @Bean
 IntegrationFlow producer() {

  // NB: this will produce twice as many
  // messages as the consumer can handle
  // w/o scaling
  MessageSource<String> messageSource = () -> (run.get() ? MessageBuilder
   .withPayload("Greetings @ " + Instant.now().toString() + ".").build() : null);
  return IntegrationFlows
   .from(messageSource, ps -> ps.poller(pm -> pm.fixedRate(1000L)))
   .channel(channels.output()).get();
 }

 public static void main(String[] args) {
  SpringApplication.run(ProducerApplication.class, args);
 }
}
