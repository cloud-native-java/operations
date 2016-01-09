package nurse.scaler.cloudfoundry;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;

/**
 * A sink that accepts two thresholds, an Cloud Foundry application ID
 * and
 *
 * @author Josh Long
 */
@ConfigurationProperties(prefix = "nurse.scaling.cloudfoundry")
public class AutoScalerSinkProperties {

    private String applicationName, metricHeaderKey;

    private Number max, min;

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getMetricHeaderKey() {
        return metricHeaderKey;
    }

    public void setMetricHeaderKey(String metricHeaderKey) {
        this.metricHeaderKey = metricHeaderKey;
    }

    public Number getMax() {
        return max;
    }

    public void setMax(Number max) {
        this.max = max;
    }

    public Number getMin() {
        return min;
    }

    public void setMin(Number min) {
        this.min = min;
    }
}