package cloudfoundry.autoscaler.sink;

import org.cloudfoundry.client.lib.CloudFoundryClient;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.cloud.stream.app.test.PropertiesInitializer;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Properties;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = AutoScalerSinkApplication.class, initializers = PropertiesInitializer.class)
@DirtiesContext
public class AutoscalerSinkTest {

 private static String APP_NAME = "configuration-client";

 // CPU%
 private static int MAX = 90;
 private static int MIN = 30;

 private static int MAX_AIS = 7;

 @Autowired
 private Sink sink;

 @Autowired
 private CloudFoundryClient client;
 private SubscribableChannel input;

 @BeforeClass
 public static void configureSink() throws Throwable {
  Properties properties = new Properties();

  // cf properties
  String prefix = "cloudfoundry.client";
  env(properties, prefix, "organization", System.getenv("CF_ORG"));
  env(properties, prefix, "apiEndpoint", System.getenv("CF_API"));
  env(properties, prefix, "space", System.getenv("CF_SPACE"));
  env(properties, prefix, "username", System.getenv("CF_USER"));
  env(properties, prefix, "password", System.getenv("CF_PASSWORD"));

  // auto-scaler properties
  prefix = "cloudfoundry.autoscaler.sink";
  env(properties, prefix, "applicationName", APP_NAME);
  env(properties, prefix, "thresholdMaximum", Integer.toString(MAX));
  env(properties, prefix, "thresholdMinimum", Integer.toString(MIN));
  env(properties, prefix, "instanceCountMaximum", Integer.toString(MAX_AIS));

  PropertiesInitializer.PROPERTIES = properties;

 }

 private static void env(Properties p, String s, String suffix, String envVarNane) {
  p.put(s + "." + suffix, envVarNane);
 }

 @Before
 public void setUp() throws Exception {
  reset();
 }

 @After
 public void tearDown() throws Exception {
  reset();
 }

 protected void reset() {
  input = this.sink.input();
  this.client.updateApplicationInstances(APP_NAME, 1);
 }

 int instances() {
  return this.client.getApplication(APP_NAME).getInstances();
 }

 @Test
 public void upAndDownWithinBounds() throws Exception {

  int start = MAX_AIS - 2;
  this.client.updateApplicationInstances(APP_NAME, start);
  Assert.assertEquals(start, instances());

  input.send(MessageBuilder.withPayload(MAX + 1).build());
  Assert.assertEquals(start + 1, instances());

  input.send(MessageBuilder.withPayload(MAX + 1).build());
  Assert.assertEquals(start + 2, instances());

  // we know that this should kick off another
  // step raise but we've capped it so it
  // shouldn't go any further
  input.send(MessageBuilder.withPayload(MAX + 1).build());
  Assert.assertEquals(start + 2, instances());

 }

 @Test
 public void upAndDown() throws Exception {

  // what we're testing:
  // this simulates a metric publishing
  // fictional CPU %.
  // we'll send that we're at just 1% over the
  // threshold
  // and once we've confirmed that the
  // auto-scaler has started 5
  // instances trying to lower the instance
  // count, we'll
  // let it off the hook by publishing a metric
  // 1% below the threshold.

  // this should publish stepping messages,
  // stepping instances counts from 1->2, 2->3,
  // 3->4

  int desiredInstanceCount = 4;
  while (instances() < desiredInstanceCount) {
   this.sink.input().send(MessageBuilder.withPayload(MAX + 1).build());
   Thread.sleep(1000 * 5);
  }
  assertEquals(instances(), desiredInstanceCount);

  // this should publish stepping messages,
  // stepping instances counts from 4->3, 3->2,
  // 2->1
  desiredInstanceCount = 1;
  while (instances() > desiredInstanceCount) {
   this.sink.input().send(MessageBuilder.withPayload(MIN - 1).build());
   Thread.sleep(1000 * 5);
  }
  assertEquals(instances(), desiredInstanceCount);
 }
}
