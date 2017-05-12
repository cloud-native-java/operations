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

    private final Object monitor = new Object();

    @EventListener(ApplicationReadyEvent.class)
    public void onAppReady(ApplicationReadyEvent event) {
        // TODO
        // 1) deploy the regular app definitions
        // 2) deploy the RMQ metrics source
        // 3) create a stream definition
        // 4) login and observe the results

        this.deployAppDefinitions();

        String rmqHost = "localhost", rmqPort = "5672", rmqUsername = "guest", rmqPw = "guest";

        String propertyPrefix = "spring.rabbitmq.";
        Map<String, String> prs = new HashMap<>();
        prs.put(propertyPrefix + "host", rmqHost);
        prs.put(propertyPrefix + "port", rmqPort);
        prs.put(propertyPrefix + "username", rmqUsername);
        prs.put(propertyPrefix + "password", rmqPw);
        StringBuffer sb = new StringBuffer();
        prs.forEach((key, value) -> sb.append("--").append(key).append('=').append(value).append(' '));
        String rmqProps = sb.toString() ;

        log.info(rmqProps);
//        String definition = "rabbit-queue-metrics --rabbitmq.metrics.queueName=testq.testq-group |   log";
        String definition1 = "rabbit-queue-metrics " +
                rmqProps + " --rabbitmq.metrics.queueName=remediation-demo.remediation-demo-group | transform --expression=payload.size  | log";


        // as configured if we start up 1 instances of the consumer then this will show the queue size is ever increasing
        // as configured if we start up 2 instances of the consumer then this will show the queue size as level/flat
        // as configured if we start up 3 instances of the consumer then this will show the queue size as draining

        // TODO configure the cloud foundry autoscaler to say that the max threshold is, say, 2 messages.
        // TODO it should scale up to equlibrium

        this.lazyDataFlowTemplate()
                .streamOperations()
                .createStream("rmq-metrics-log",
                        definition1, true);
    }

    private void deployAppDefinitions() {
        List<String> apps = new ArrayList<>();
        apps.add("http://repo.spring.io/libs-release-local/org/springframework/cloud/task/app/spring-cloud-task-app-descriptor/Addison.RELEASE/spring-cloud-task-app-descriptor-Addison.RELEASE.task-apps-maven");
        apps.add("http://repo.spring.io/libs-release/org/springframework/cloud/stream/app/spring-cloud-stream-app-descriptor/Avogadro.SR1/spring-cloud-stream-app-descriptor-Avogadro.SR1.stream-apps-rabbit-maven");
        apps.add("http://localhost:9494/apps.properties");
        apps
                .parallelStream()
                .forEach(s -> lazyDataFlowTemplate()
                        .appRegistryOperations().importFromResource(s, true));
    }

    private DataFlowTemplate lazyDataFlowTemplate() {
        synchronized (this.monitor) {
            if (null == this.dataFlowTemplate) {
                this.dataFlowTemplate = new DataFlowTemplate(URI.create("http://localhost:9494"), new RestTemplate());
                this.log.info("created new " + DataFlowTemplate.class.getName());
            }
            return this.dataFlowTemplate;
        }
    }


}
