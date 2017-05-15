package cloudfoundry.autoscaler.sink;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudfoundry.operations.CloudFoundryOperations;
import org.cloudfoundry.operations.applications.ApplicationDetail;
import org.cloudfoundry.operations.applications.GetApplicationRequest;
import org.cloudfoundry.operations.applications.ScaleApplicationRequest;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

public class AutoScalerMessageHandler implements MessageHandler {

 private final Number icMin, icMax, maxNumber, minNumber;

 private final String applicationName, metricHeaderKey;

 private final CloudFoundryOperations client;

 private Log log = LogFactory.getLog(getClass());

 private RetryTemplate retryTemplate = new RetryTemplate();

 public AutoScalerMessageHandler(CloudFoundryOperations client,
  Number minNumber, Number maxNumber, Number icMin, Number icMax,
  String metricHeaderKey, String appName) {

  this.client = client;
  this.icMax = icMax;
  this.icMin = icMin;
  this.maxNumber = maxNumber == null ? 0 : maxNumber;
  this.minNumber = minNumber == null ? 0 : minNumber;
  this.applicationName = appName;
  this.metricHeaderKey = metricHeaderKey;
 }

 @Override
 public void handleMessage(Message<?> message) throws MessagingException {

  String key = this.metricHeaderKey;

  Object incomingValue = (StringUtils.hasText(key) && message.getHeaders()
   .containsKey(key)) ? message.getHeaders().get(key) : message.getPayload();

  Assert.isTrue(Number.class.isAssignableFrom(incomingValue.getClass()),
   "the metric must be assignable to a number!");
  Number incomingNumber = Number.class.cast(incomingValue);

  // two simple cases
  double v = incomingNumber.doubleValue();
  double max = maxNumber.doubleValue();
  double min = minNumber.doubleValue();

  log.info("incoming value: " + v);
  log.info("max: " + max);
  log.info("min: " + min);

  // suppose the CPU is 90% and the max
  // tolerable threshold is 70%, then we
  // need to
  // add more capacity, step up.
  if (v > max) {
   log.info("v > max");
   scale(this.applicationName, 1);
  }

  // suppose the CPU is 10% and the min
  // tolerable threshold is 20%, then we
  // have
  // too much capacity, step down.
  if (v < min) {
   log.info("v < max");
   scale(this.applicationName, -1);
  }
 }

 private int instances(String appName) {

  ApplicationDetail details = this.client.applications()
   .get(GetApplicationRequest.builder().name(appName).build()).block();
  return details.getInstances();
 }

 protected void scale(String appName, int delta) throws MessagingException {
  int currentCount = this.instances(appName);
  this.log.info("count before scale: " + currentCount);
  int newSum = currentCount + delta;
  if (isWithinBounds(newSum)) {
   this.scaleAppInstance(appName, newSum);
   log.info(String.format("updating application %s " + "instances %s", appName,
    newSum));
  }
 }

 private void scaleAppInstance(String appName, int count) {
  this.retryTemplate.execute(ctx -> {
   client
    .applications()
    .scale(
     ScaleApplicationRequest.builder().name(appName).instances(count).build())
    .block();
   return null;
  });
 }

 protected boolean isWithinBounds(int newSum) {

  int baseline = 1, roof = 0;

  if (this.icMin != null && !this.icMin.equals(0)) {
   baseline = this.icMin.intValue();
  }

  if (this.icMax != null && !this.icMax.equals(0)) {
   roof = this.icMax.intValue();
  }

  boolean ltBaseline = (newSum < baseline);
  // if the roof is 0, the default, then
  // it's
  // fine to keep going up
  boolean gtCeiling = roof != 0 && newSum > roof;
  return !ltBaseline && !gtCeiling;
 }
}