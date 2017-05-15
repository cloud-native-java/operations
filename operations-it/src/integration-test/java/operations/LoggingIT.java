package operations;

import cnj.CloudFoundryService;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.net.URI;
import java.util.stream.Stream;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = LoggingIT.Config.class)
public class LoggingIT {

 private File root, loggingManifest;

 @Autowired
 private CloudFoundryService cloudFoundryService;

 private final Log log = LogFactory.getLog(getClass());

 private final RestTemplate restTemplate = new RestTemplate();

 private void exists(File f) {
  Assert.assertTrue(f.getAbsolutePath() + " does not exist!", f.exists());
 }

 @Before
 public void before() throws Throwable {
  this.root = new File(".");
  this.loggingManifest = new File(this.root, "../logging/manifest.yml");
  Stream.of(this.loggingManifest).forEach(this::exists);
  this.cloudFoundryService.pushApplicationUsingManifest(this.loggingManifest);
 }

 private String level(String uri) {
  ResponseEntity<JsonNode> responseEntity = this.restTemplate.getForEntity(
   URI.create(uri), JsonNode.class);
  JsonNode body = responseEntity.getBody();
  JsonNode configuredLevel = body.get("configuredLevel");
  return configuredLevel.asText();
 }

 @Test
 public void logging() throws Throwable {
  String loggingApp = "logging-application";
  String appUrl = this.cloudFoundryService.urlForApplication(loggingApp);
  String loggersUrl = appUrl + "/loggers/demo";
  log.info(loggersUrl);
  Assert.assertEquals(level(loggersUrl), "ERROR");
  RequestEntity<String> entity = RequestEntity.post(URI.create(loggersUrl))
   .contentType(MediaType.APPLICATION_JSON)
   .body("{ \"configuredLevel\": \"DEBUG\" }");
  this.restTemplate.exchange(entity, Void.class);
  Assert.assertEquals(level(loggersUrl), "DEBUG");
 }

 @Configuration
 @EnableAutoConfiguration
 public static class Config {
 }
}
