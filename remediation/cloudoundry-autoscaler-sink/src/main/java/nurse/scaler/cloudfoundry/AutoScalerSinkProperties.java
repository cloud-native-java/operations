package nurse.scaler.cloudfoundry;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * A sink that accepts two thresholds, an Cloud Foundry application ID
 * and
 *
 * @author Josh Long
 */
@ConfigurationProperties(prefix = "nurse.scaling.cloudfoundry")
public class AutoScalerSinkProperties {

    private String applicationName, metricHeaderKey;

    private Number thresholdMaximum, thresholdMinimum;

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

    public Number getThresholdMaximum() {
        return thresholdMaximum;
    }

    public void setThresholdMaximum(Number thresholdMaximum) {
        this.thresholdMaximum = thresholdMaximum;
    }

    public Number getThresholdMinimum() {
        return thresholdMinimum;
    }

    public void setThresholdMinimum(Number thresholdMinimum) {
        this.thresholdMinimum = thresholdMinimum;
    }
}