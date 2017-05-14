package cnj;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.embedded.EmbeddedServletContainerInitializedEvent;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.*;


public class MetricsSpanReporterAutoConfigurationTest {

 private static final Map<String, Integer> contexts = new ConcurrentHashMap<>();

 private static final Log log = LogFactory
  .getLog(MetricsSpanReporterAutoConfigurationTest.class);

 private static CountDownLatch countDownLatch = new CountDownLatch(2);

 private ApplicationContext a, b;

 private RestTemplate restTemplate = new RestTemplate();

 @Before
 public void before() throws Exception {

  this.a = SpringApplication.run(A.class);
  this.b = SpringApplication.run(B.class);

  countDownLatch.await();

  log.info("the applications are ready!");
  assertEquals(contexts.size(), 2);
  contexts.entrySet().forEach(
   e -> {

    ResponseEntity<Map<String, String>> responseEntity = this.restTemplate
     .exchange("http://localhost:" + e.getValue() + "/hi", HttpMethod.GET,
      null, new ParameterizedTypeReference<Map<String, String>>() {
      });

    Map<String, String> body = responseEntity.getBody();
    log.info("result from calling '" + e.getKey() + "': " + body);
    String message = body.get("message");
    assertNotNull(message);
    assertTrue(message.contains("Hi from"));
   });

  ParameterizedTypeReference<Map<String, Object>> ptr = new ParameterizedTypeReference<Map<String, Object>>() {
  };

  int portForB = contexts.get("b"), portForA = contexts.get("a");

  String url = "http://localhost:" + portForB + "/service";
  for (int i = 0; i < 10; i++) {
   Map<String, Object> msg = this.restTemplate.exchange(url, HttpMethod.GET,
    null, ptr).getBody();
   assertEquals(msg.get("message"), "Hello, client!");
  }
  Arrays.asList(portForA, portForB).forEach(
   port -> {
    ResponseEntity<Map<String, Object>> entity = this.restTemplate.exchange(
     "http://localhost:" + port + "/metrics", HttpMethod.GET, null, ptr);
    Map<String, Object> map = entity.getBody();
    Object metrics98thPercentile = map
     .get("timer.spans.http:/hi.snapshot.98thPercentile");
    assertTrue("the 98th percentile should be non-zero!",
     Double.parseDouble("" + metrics98thPercentile) > 0);
    log.info(entity);
   });

 }

 @Test
 public void testMetrics() throws Exception {

 }

 public abstract static class Base {

  protected abstract String getIdentifier();

  @RequestMapping("/hi")
  Map<String, String> hi() {
   return Collections.singletonMap("message", "Hi from " + getIdentifier());
  }

  @Bean
  ESCAL escal() {
   return new ESCAL(getIdentifier());
  }

  @Component
  public static class CDLAL {

   @EventListener(ApplicationReadyEvent.class)
   public void applicationReady(ApplicationReadyEvent event) {
    countDownLatch.countDown();
   }
  }

  public static class ESCAL {

   private final String identifier;

   public ESCAL(String x) {
    this.identifier = x;
   }

   @EventListener(EmbeddedServletContainerInitializedEvent.class)
   public void containerInitialized(EmbeddedServletContainerInitializedEvent e) {
    int port = e.getEmbeddedServletContainer().getPort();
    contexts.put(identifier, port);
   }
  }

 }

 @EnableAutoConfiguration
 @Configuration
 @RestController
 public static class A extends Base {

  private RestTemplate restTemplate = new RestTemplate();

  @Override
  protected String getIdentifier() {
   return "a";
  }

  @RequestMapping("/client")
  ResponseEntity<String> client(@RequestParam String service) {
   return ResponseEntity.ok(this.restTemplate.getForEntity(service,
    String.class).getBody());
  }
 }

 @EnableAutoConfiguration
 @Configuration
 @RestController
 public static class B extends Base {

  @RequestMapping("/service")
  public Map<String, String> hello() {
   return Collections.singletonMap("message", "Hello, client!");
  }

  @Override
  protected String getIdentifier() {
   return "b";
  }
 }
}