package demo;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.graphite.Graphite;
import com.codahale.metrics.graphite.GraphiteReporter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.ExportMetricWriter;
import org.springframework.boot.actuate.metrics.statsd.StatsdMetricWriter;
import org.springframework.boot.actuate.metrics.writer.MetricWriter;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import javax.annotation.PostConstruct;
import java.net.URL;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
public class ActuatorApplication {

	@PostConstruct
	public void begin() throws Exception {
		// prevent DNS caching because
		// HostedGraphite nodes may move
		java.security.Security.setProperty("networkaddress.cache.ttl", "60"); // <1>
	}

//	@Bean
//	@ExportMetricWriter
//	MetricWriter metricWriter(
//			@Value("${hostedGraphite.apiKey}") String key,
//			@Value("${hostedGraphite.url}") String host,
//			@Value("${hostedGraphite.port}") int port) {
//		return new StatsdMetricWriter(key, host, port);
//	}


	@Bean
	GraphiteReporter graphiteWriter(MetricRegistry registry) {

		// prevent DNS caching because
		// HostedGraphite nodes may move
		java.security.Security.setProperty("networkaddress.cache.ttl", "60"); // <1>

		int port = 2003;
		String apiKey = "b1f80e86-35ad-458b-a391-1ba8d6533e99";
		String host = "672c4ee8.carbon.hostedgraphite.com";

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

