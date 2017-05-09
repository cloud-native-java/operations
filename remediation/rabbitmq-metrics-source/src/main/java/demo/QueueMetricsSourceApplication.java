package demo;


import org.springframework.amqp.rabbit.core.RabbitOperations;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.app.trigger.TriggerConfiguration;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.core.Pollers;
import org.springframework.integration.scheduling.PollerMetadata;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.Trigger;

@SpringBootApplication
@Import(TriggerConfiguration.class)
@EnableConfigurationProperties(QueueMonitorProperties.class)
@EnableBinding(Source.class)
public class QueueMetricsSourceApplication {

    private final Trigger trigger;
    private final String queueName;

    public QueueMetricsSourceApplication(Trigger t, QueueMonitorProperties queueMonitorProperties) {
        this.queueName = queueMonitorProperties.getQueueName();
        this.trigger = t;
    }

    @Bean
    PollerMetadata poller() {
        return Pollers.trigger(trigger).get();
    }

    @Bean
    QueueMonitor monitor(RabbitOperations rabbitOperations) {
        return new QueueMonitor(rabbitOperations);
    }

    @Bean
    IntegrationFlow rabbitMqStatisticsSource(QueueMonitor monitor, Source source) {
        MessageChannel output = source.output();
        MessageSource<QueueStatistics> qms = () -> MessageBuilder
                .withPayload(monitor.getQueueStatistics(this.queueName))
                .build();
        return IntegrationFlows
                .from(qms, sp -> sp.poller(poller()))
                .channel(output)
                .get();
    }
}

