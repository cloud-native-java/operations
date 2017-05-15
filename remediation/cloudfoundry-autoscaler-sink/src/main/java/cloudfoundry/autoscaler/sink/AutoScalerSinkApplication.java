package cloudfoundry.autoscaler.sink;

import org.cloudfoundry.operations.CloudFoundryOperations;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;

@EnableBinding(Sink.class)
@EnableConfigurationProperties(AutoScalerSinkProperties.class)
@SpringBootApplication
public class AutoScalerSinkApplication {

 @Bean
 public IntegrationFlow inboundScaleDecisionFlow(
  CloudFoundryOperations operations, AutoScalerSinkProperties properties)
  throws Exception {

  AutoScalerMessageHandler messageHandler = new AutoScalerMessageHandler(
   operations, properties.getThresholdMinimum(),
   properties.getThresholdMaximum(), properties.getInstanceCountMinimum(),
   properties.getInstanceCountMaximum(), properties.getMetricHeaderKey(),
   properties.getApplicationName());

  return IntegrationFlows.from(Sink.INPUT).handle(messageHandler).get();
 }

 public static void main(String[] args) throws Exception {
  SpringApplication.run(AutoScalerSinkApplication.class, args);
 }
}
