package cloudfoundry.autoscaler.sink;

import cnj.CloudFoundryService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudfoundry.operations.CloudFoundryOperations;
import org.cloudfoundry.operations.applications.GetApplicationRequest;
import org.cloudfoundry.operations.applications.ScaleApplicationRequest;
import org.cloudfoundry.operations.applications.StartApplicationRequest;
import org.cloudfoundry.operations.applications.StopApplicationRequest;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.StreamUtils;

import java.io.*;
import java.nio.file.Files;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ScaleSinkIT.Application.class)
public class ScaleSinkIT {

 private static int MAX = 90;

 private static int MIN = 30;

 private static int MAX_AIS = 7;

 private RetryTemplate retryTemplate = new RetryTemplate();

 @Autowired
 private Sink sink;

 @Autowired
 private CloudFoundryOperations ops;

 @Autowired
 private AutoScalerSinkProperties props;

 @Autowired
 private CloudFoundryService cloudFoundryService;

 private String applicationName;

 private Log log = LogFactory.getLog(getClass());

 private File tempRootForStaging;

 private File manifestFile;

 private File jarFile;

 private void init() throws Throwable {

  this.tempRootForStaging = Files.createTempDirectory("staging").toFile();
  this.tempRootForStaging.deleteOnExit();
  this.manifestFile = new File(tempRootForStaging, "manifest.yml");
  this.jarFile = new File(tempRootForStaging, "hi.jar");
 }

 @After
 public void after() throws Throwable {

  if (null != tempRootForStaging && tempRootForStaging.exists())
   tempRootForStaging.delete();

  if (cloudFoundryService.applicationExists(this.applicationName)) {
   this.ops.applications()
    .stop(StopApplicationRequest.builder().name(applicationName).build())
    .block();
  }

 }

 @Before
 public void before() throws Throwable {
  this.init();
  this.deploySampleApp();
  if (cloudFoundryService.applicationExists(this.applicationName)) {
   this.ops
    .applications()
    .start(StartApplicationRequest.builder().name(this.applicationName).build())
    .block();
  }
  this.scale(this.applicationName, 1);
 }

 private void stage() throws Throwable {
  Assert.assertTrue(tempRootForStaging.exists() || tempRootForStaging.mkdirs());
  Resource manifest = new ClassPathResource("/manifest.yml");
  Resource jar = new FileSystemResource(new File(
   "./src/integration-test/resources/hi.jar"));
  try (InputStream mi = manifest.getInputStream();
   InputStream ji = jar.getInputStream();
   OutputStream mo = new FileOutputStream(manifestFile);
   OutputStream jo = new FileOutputStream(jarFile)) {
   StreamUtils.copy(mi, mo);
   StreamUtils.copy(ji, jo);
  }
  log.info("staging application at " + tempRootForStaging.getAbsolutePath());

  Assert.assertTrue(
   "the hi.jar should exist at " + this.jarFile.getAbsolutePath(),
   this.jarFile.exists());

  Assert.assertTrue(
   "the manifest.yml should exist at " + this.manifestFile.getAbsolutePath(),
   this.manifestFile.exists());
 }

 private void deploySampleApp() throws Throwable {
  this.stage();

  this.applicationName = this.cloudFoundryService
   .applicationManifestFrom(this.manifestFile).entrySet().stream()
   .map(e -> e.getValue().getName()).findAny().orElse(null);
  if (!this.cloudFoundryService.applicationExists(this.applicationName)) {
   this.cloudFoundryService.pushApplicationUsingManifest(this.manifestFile);
  }
 }

 @Test
 public void upAndDown() throws Exception {

  // what we're testing:
  // this simulates a metric publishing
  // fictional CPU %.
  // we'll send that we're at just 1%
  // over the
  // threshold
  // and once we've confirmed that the
  // auto-scaler has started 5
  // instances trying to lower the
  // instance
  // count, we'll let it off the hook by
  // publishing a metric
  // 1% below the threshold.

  // this should publish stepping
  // messages,
  // stepping instances counts from
  // 1->2, 2->3,
  // 3->4

  int desiredInstanceCount = 4;
  while (instances() < desiredInstanceCount) {
   this.sink.input().send(MessageBuilder.withPayload(MAX + 1).build());
  }
  Assert.assertEquals(instances(), desiredInstanceCount);

  // this should publish stepping
  // messages,
  // stepping instances counts from
  // 4->3, 3->2, 2->1
  desiredInstanceCount = 1;
  while (instances() > desiredInstanceCount) {
   this.sink.input().send(MessageBuilder.withPayload(MIN - 1).build());
  }
  Assert.assertEquals(instances(), desiredInstanceCount);
 }

 @Test
 public void upAndDownWithinBounds() throws Exception {
  SubscribableChannel input = this.sink.input();
  int start = MAX_AIS - 2;

  scale(this.props.getApplicationName(), start);
  Assert.assertEquals(start, instances());

  input.send(MessageBuilder.withPayload(MAX + 1).build());
  Assert.assertEquals(start + 1, instances());

  input.send(MessageBuilder.withPayload(MAX + 1).build());
  Assert.assertEquals(start + 2, instances());

  // we know that this should kick off
  // another
  // step raise but we've capped it so
  // it
  // shouldn't go any further
  input.send(MessageBuilder.withPayload(MAX + 1).build());
  Assert.assertEquals(start + 2, instances());

 }

 private int instances() {
  String appName = this.props.getApplicationName();
  return this.ops.applications()
   .get(GetApplicationRequest.builder().name(appName).build()).block()
   .getInstances();
 }

 private void scale(String appName, int count) {
  retryTemplate.execute(ctx -> {
   this.ops
    .applications()
    .scale(
     ScaleApplicationRequest.builder().name(appName).instances(count).build())
    .block();
   return null;
  });
 }

 @SpringBootApplication
 public static class Application {
 }
}
