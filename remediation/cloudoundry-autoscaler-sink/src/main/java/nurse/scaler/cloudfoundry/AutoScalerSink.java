package nurse.scaler.cloudfoundry;

import org.cloudfoundry.client.lib.CloudFoundryClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;

/**
 * A sink that accepts two polar thresholds and scales a Cloud Foundry application stepwise,
 * up or down based on whether incoming messages contain a value in, below, or above those thresholds.
 *
 * @author Josh Long
 */
@EnableBinding(Sink.class)
@EnableConfigurationProperties({AutoScalerSinkProperties.class})
public class AutoScalerSink {

    @Bean
    public IntegrationFlow inboundMetricFlow(
            CloudFoundryClient client,
            AutoScalerSinkProperties properties) throws Exception {

        Number minNumber = properties.getThresholdMinimum(),
                maxNumber = properties.getThresholdMaximum();

        AutoScalerMessageHandler messageHandler = new AutoScalerMessageHandler(
                client,
                minNumber,
                maxNumber,
                properties.getMetricHeaderKey(),
                properties.getApplicationName());

        return IntegrationFlows.from(Sink.INPUT)
                .handle(messageHandler)
                .get();
    }
}