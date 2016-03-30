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

	@PostConstruct
	public void begin() throws Exception {
		// prevent DNS caching because HostedGraphite nodes may move
		java.security.Security.setProperty("networkaddress.cache.ttl", "60"); // <1>
	}

	@Bean
	GraphiteReporter graphiteWriter(
			@Value("${hostedGraphite.apiKey}") String apiKey, // NB: we're using the API KEY as a prefix
			@Value("${hostedGraphite.url}") String host,
			@Value("${hostedGraphite.port}") int port,
			MetricRegistry registry) {

		GraphiteReporter reporter = GraphiteReporter.forRegistry(registry)
				.prefixedWith(apiKey) // <2>
				.build(new Graphite(host, port));
		reporter.start(2, TimeUnit.SECONDS);
		return reporter;
	}

	public static void main(String[] args) {
		SpringApplication.run(ActuatorApplication.class, args);
	}
}

