package operations;

import cnj.CloudFoundryService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudfoundry.operations.CloudFoundryOperations;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.net.URI;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TraceIT.Config.class)
public class TraceIT {

 @Autowired
 private CloudFoundryService cloudFoundryService;

 private File root = new File(".");

 private File zipkinServiceManifest, clientAManifest, clientBManifest;

 @Before
 public void init() throws Throwable {
  String amqpServiceInstance = "cnj-trace-rabbitmq";
  String mysqlServiceInstance = "cnj-trace-mysql";

  this.cloudFoundryService.createServiceIfMissing("cloudamqp", "lemur",
   amqpServiceInstance);
  this.cloudFoundryService.createServiceIfMissing("p-mysql", "100mb",
   mysqlServiceInstance);

  this.zipkinServiceManifest = new File(root,
   "../tracing/zipkin-service/manifest.yml");
  this.clientAManifest = new File(root,
   "../tracing/zipkin-client-a/manifest.yml");
  this.clientBManifest = new File(root,
   "../tracing/zipkin-client-b/manifest.yml");

  Assert.assertTrue(zipkinServiceManifest.exists());
  Assert.assertTrue(clientAManifest.exists());
  Assert.assertTrue(clientBManifest.exists());
 }

 @Test
 public void traceServices() throws Throwable {

  // first deploy zipkin-service as app
  // AND service
  // then deploy zipkin-client-b as app
  // AND a service
  // then deploy zipkin-client-a and
  // have it bind to b

  this.cloudFoundryService
   .pushApplicationAndCreateUserDefinedServiceUsingManifest(this.zipkinServiceManifest);

  this.cloudFoundryService
   .pushApplicationAndCreateUserDefinedServiceUsingManifest(this.clientBManifest);

  this.cloudFoundryService.pushApplicationUsingManifest(this.clientAManifest);

  ResponseEntity<String> entity = this.restTemplate.getForEntity(
   URI.create(cloudFoundryService.urlForApplication("zipkin-client-a")),
   String.class);

  String body = entity.getBody().toLowerCase();
  this.log.info("traced response: " + body);
  Assert.assertTrue(body.contains("x-span-name"));
  Assert.assertTrue(body.contains("hi, "));
 }

 private Log log = LogFactory.getLog(getClass());

 @Autowired
 private RestTemplate restTemplate;

 @Configuration
 @EnableAutoConfiguration
 public static class Config {

  @Bean
  RestTemplate restTemplate() {
   return new RestTemplate();
  }
 }
}
