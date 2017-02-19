package cloudfoundry.autoscaler.sink;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * A sink that accepts two thresholds, an Cloud
 * Foundry application ID and
 *
 * @author Josh Long
 */
@ConfigurationProperties(prefix = "cloudfoundry.autoscaler.sink")
public class AutoScalerSinkProperties {

 private String applicationName, metricHeaderKey;

 private Number thresholdMaximum, thresholdMinimum;

 private Number instanceCountMinimum = 0;

 private Number instanceCountMaximum = 0;

 public Number getInstanceCountMinimum() {
  return instanceCountMinimum;
 }

 public void setInstanceCountMinimum(Number instanceCountMinimum) {
  this.instanceCountMinimum = instanceCountMinimum;
 }

 public Number getInstanceCountMaximum() {
  return instanceCountMaximum;
 }

 public void setInstanceCountMaximum(Number instanceCountMaximum) {
  this.instanceCountMaximum = instanceCountMaximum;
 }

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