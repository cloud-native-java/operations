package demo;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.graphite.Graphite;
import com.codahale.metrics.graphite.GraphiteReporter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
public class ActuatorApplication {

 public static void main(String[] args) {
  SpringApplication.run(ActuatorApplication.class, args);
 }

 @Bean
 GraphiteReporter graphiteWriter(
  @Value("${hostedGraphite.apiKey}") String apiKey, // NB:
  @Value("${hostedGraphite.url}") String host,
  @Value("${hostedGraphite.port}") int port,
  @Value("${graphite.reporter.period:2}") int period, MetricRegistry registry) {

  java.security.Security.setProperty("networkaddress.cache.ttl", "60"); // <1>

  GraphiteReporter reporter = GraphiteReporter.forRegistry(registry)
   .prefixedWith(apiKey) // <2>
   .build(new Graphite(host, port));
  reporter.start(period, TimeUnit.SECONDS);
  return reporter;
 }
}
