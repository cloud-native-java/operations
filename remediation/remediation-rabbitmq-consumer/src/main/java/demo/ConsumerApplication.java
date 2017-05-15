package demo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;

@EnableBinding(Sink.class)
@SpringBootApplication
public class ConsumerApplication {

 @Bean
 IntegrationFlow incoming(Sink channels) {
  Log log = LogFactory.getLog(getClass());
  return IntegrationFlows
   .from(channels.input())
   .handle(
    message -> {
     try {
      long duration = 2000L;
      log.info("sleeping " + duration
       + "ms to simulate long-running processing");
      Thread.sleep(duration);
     }
     catch (InterruptedException e) {
      throw new RuntimeException(e);
     }
     log.info("new message! " + message.getPayload());
    }).get();
 }

 public static void main(String args[]) {
  SpringApplication.run(ConsumerApplication.class, args);
 }
}
