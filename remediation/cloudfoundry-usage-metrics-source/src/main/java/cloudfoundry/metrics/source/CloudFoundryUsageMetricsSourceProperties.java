package cloudfoundry.metrics.source;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cloudfoundry.metrics.source")
public class CloudFoundryUsageMetricsSourceProperties {

 private String applicationName;

 public String getApplicationName() {
  return applicationName;
 }

 public void setApplicationName(String applicationName) {
  this.applicationName = applicationName;
 }
}
