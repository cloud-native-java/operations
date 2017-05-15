package demo;

import com.rabbitmq.client.AMQP;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.amqp.rabbit.core.RabbitOperations;

public class QueueMonitor {

 private Log log = LogFactory.getLog(getClass());

 private final RabbitOperations rabbitOperations;

 public QueueMonitor(RabbitOperations rabbitOperations) {
  this.rabbitOperations = rabbitOperations;
 }

 public QueueStatistics getQueueStatistics(String q) {

  String qu = "'" + q + "'";

  this.log.info("attempting to gather statistics for queue " + qu + ".");

  return this.rabbitOperations.execute(channel -> {

   log.info("about to declare a passive queue for " + qu);

   AMQP.Queue.DeclareOk queueInfo = channel.queueDeclarePassive(q);

   log.info("AMQP.Queue.DeclareOk is null? "
    + (queueInfo == null ? "yes" : "no"));

   return new QueueStatistics(q, queueInfo.getMessageCount(), queueInfo
    .getConsumerCount());
  });
 }
}
