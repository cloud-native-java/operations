package demo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cloud.dataflow.rest.client.DataFlowTemplate;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Configuration
public class DataFlowInitializer {

 private final Log log = LogFactory.getLog(getClass());

 private DataFlowTemplate dataFlowTemplate;

 private String demoRabbitMqServiceName = "remediation-rmq";

 private final Object monitor = new Object();

 String cf() {
  StringBuilder res = new StringBuilder();
  System.getenv().forEach(
   (k, v) -> {
    if (k.toLowerCase().contains("cf_")) {
     res.append(" --").append(k.toLowerCase().replace("_", ".")).append('=')
      .append(v);
    }
   });
  return res.toString();
 }

 @EventListener(ApplicationReadyEvent.class)
 public void onAppReady(ApplicationReadyEvent evt) {

  this.deployAppDefinitions();

  this.lazyDataFlowTemplate().streamOperations().destroyAll();

  String streamDefinition = "rabbit-queue-metrics --management.security.enabled=false  --rabbitmq.metrics.queueName=remediation-demo.remediation-demo-group "
   + "| transform --expression=headers['queue-size'] "
   + "| cloudfoundry-autoscaler "
   + cf()
   + " --cloudfoundry.autoscaler.sink.instanceCountMinimum=1 "
   + " --cloudfoundry.autoscaler.sink.applicationName=remediation-rabbitmq-consumer "
   + " --cloudfoundry.autoscaler.sink.instanceCountMaximum=10 "
   + " --cloudfoundry.autoscaler.sink.thresholdMaximum=5 ";

  this.lazyDataFlowTemplate().streamOperations()
   .createStream("rmq-metrics-log", streamDefinition, true);
 }

 private void deployAppDefinitions() {
  List<String> apps = new ArrayList<>();
  apps
   .add("http://repo.spring.io/libs-release-local/org/springframework/cloud/task/app/spring-cloud-task-app-descriptor/Addison.RELEASE/spring-cloud-task-app-descriptor-Addison.RELEASE.task-apps-maven");
  apps
   .add("http://repo.spring.io/libs-release/org/springframework/cloud/stream/app/spring-cloud-stream-app-descriptor/Avogadro.SR1/spring-cloud-stream-app-descriptor-Avogadro.SR1.stream-apps-rabbit-maven");
  apps.add("http://localhost:9494/apps.properties");
  apps.parallelStream().forEach(
   s -> lazyDataFlowTemplate().appRegistryOperations().importFromResource(s,
    true));
 }

 private DataFlowTemplate lazyDataFlowTemplate() {
  synchronized (this.monitor) {
   if (null == this.dataFlowTemplate) {
    this.dataFlowTemplate = new DataFlowTemplate(
     URI.create("http://localhost:9494"), new RestTemplate());
    this.log.info("created new " + DataFlowTemplate.class.getName());
   }
   return this.dataFlowTemplate;
  }
 }

}
