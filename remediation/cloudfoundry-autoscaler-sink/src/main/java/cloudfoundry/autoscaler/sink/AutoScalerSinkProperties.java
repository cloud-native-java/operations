package cloudfoundry.autoscaler.sink;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ConfigurationProperties(prefix = "cloudfoundry.autoscaler.sink")
public class AutoScalerSinkProperties {

 private String applicationName, metricHeaderKey;

 private Number thresholdMaximum, thresholdMinimum;

 private Number instanceCountMinimum = 0;

 private Number instanceCountMaximum = 0;
}