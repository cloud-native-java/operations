package demo;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(value = "rabbitmq.metrics.source")
public class QueueMonitorProperties {

    private final String queueName ;
}

