package demo;


import org.springframework.amqp.rabbit.core.RabbitOperations;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.app.trigger.TriggerConfiguration;
import org.springframework.cloud.stream.app.trigger.TriggerPropertiesMaxMessagesDefaultUnlimited;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.core.Pollers;
import org.springframework.integration.json.ObjectToJsonTransformer;
import org.springframework.integration.scheduling.PollerMetadata;
import org.springframework.integration.transformer.ObjectToMapTransformer;
import org.springframework.integration.transformer.Transformer;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.Trigger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SpringBootApplication
@Import({TriggerConfiguration.class, TriggerPropertiesMaxMessagesDefaultUnlimited.class})
@EnableConfigurationProperties(QueueMonitorProperties.class)
@EnableBinding(Source.class)
public class QueueMetricsSourceApplication {

    private final String queueName;

    public QueueMetricsSourceApplication(QueueMonitorProperties queueMonitorProperties) {
        this.queueName = queueMonitorProperties.getQueueName();
    }

    @Bean
    QueueMonitorProperties queueMonitorProperties() {
        return new QueueMonitorProperties();
    }

    @Bean
    QueueMonitor monitor(RabbitOperations rabbitOperations) {
        return new QueueMonitor(rabbitOperations);
    }

    @Bean
    IntegrationFlow rabbitMqStatisticsSource(PollerMetadata pollerMetadata,
                                             QueueMonitor monitor, Source source) {
        MessageChannel output = source.output();
        MessageSource<QueueStatistics> qms = () -> MessageBuilder
                .withPayload(monitor.getQueueStatistics(this.queueName))
                .build();
        return IntegrationFlows
                .from(qms, sp -> sp.poller(pollerMetadata))
                .transform((Transformer) message -> {
                    Object payload = message.getPayload();
                    QueueStatistics statistics = QueueStatistics.class.cast(payload);
                    Map<String, Object> statsMap = new ConcurrentHashMap<>();
                    statsMap.put("queue", statistics.getQueue());
                    statsMap.put("consumers", statistics.getConsumers());
                    statsMap.put("size", statistics.getSize());
                    return MessageBuilder
                            .withPayload(statsMap)
                            .copyHeadersIfAbsent(message.getHeaders())
                            .build();
                })
                .channel(output)
                .get();
    }

    public static void main(String args[]) {
        SpringApplication.run(QueueMetricsSourceApplication.class, args);
    }
}

