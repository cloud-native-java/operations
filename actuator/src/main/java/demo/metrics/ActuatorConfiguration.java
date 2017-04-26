package demo.metrics;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.graphite.Graphite;
import com.codahale.metrics.graphite.GraphiteReporter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

@Configuration
class ActuatorConfiguration {

 ActuatorConfiguration() {
  java.security.Security.setProperty("networkaddress.cache.ttl", "60"); // <1>
 }

 @Bean
 GraphiteReporter graphiteWriter(
  @Value("${hostedGraphite.apiKey}") String apiKey,
  @Value("${hostedGraphite.url}") String host,
  @Value("${hostedGraphite.port}") int port, MetricRegistry registry) {

  GraphiteReporter reporter = GraphiteReporter.forRegistry(registry)
   .prefixedWith(apiKey) // <2>
   .build(new Graphite(host, port));
  reporter.start(1, TimeUnit.SECONDS);
  return reporter;
 }

}
