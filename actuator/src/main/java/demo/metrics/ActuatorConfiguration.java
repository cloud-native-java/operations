package demo.metrics;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.graphite.Graphite;
import com.codahale.metrics.graphite.GraphiteReporter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.ExportMetricWriter;
import org.springframework.boot.actuate.metrics.repository.redis.RedisMetricRepository;
import org.springframework.boot.actuate.metrics.writer.MetricWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.util.concurrent.TimeUnit;

@Configuration
class ActuatorConfiguration {

    // todo
    @Bean
    @Profile("graphite")
    GraphiteReporter graphiteReporter(MetricRegistry registry,
                                      @Value("${graphite.host}") String host,
                                      @Value("${graphite.port}") int port,
                                      @Value("${graphite.reporter.period}") int period) {

        GraphiteReporter reporter = GraphiteReporter
                .forRegistry(registry)
                .prefixedWith("cnj.metrics")
                .build(new Graphite(host, port));

        reporter.start(period, TimeUnit.MILLISECONDS);
        return reporter;
    }


    @Bean
    @ExportMetricWriter
    MetricWriter metricWriter(RedisConnectionFactory cf) {
        return new RedisMetricRepository(cf);
    }
}
