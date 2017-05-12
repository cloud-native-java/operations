package demo;


import com.rabbitmq.client.AMQP;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.amqp.rabbit.core.RabbitOperations;

public class QueueMonitor {

    private final RabbitOperations rabbitOperations;

    public QueueMonitor (RabbitOperations rabbitOperations) {
        this.rabbitOperations = rabbitOperations;
    }

    public QueueStatistics getQueueStatistics(String q) {
        return this.rabbitOperations.execute(channel -> {
            AMQP.Queue.DeclareOk queueInfo = channel.queueDeclarePassive(q);
            return new QueueStatistics(q, queueInfo.getMessageCount(),
                    queueInfo.getConsumerCount());
        });
    }
}

