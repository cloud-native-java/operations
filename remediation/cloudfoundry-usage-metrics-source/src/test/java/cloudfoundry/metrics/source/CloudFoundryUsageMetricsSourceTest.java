package cloudfoundry.metrics.source;

import cnj.CloudFoundryService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudfoundry.operations.CloudFoundryOperations;
import org.cloudfoundry.operations.applications.GetApplicationRequest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.messaging.Message;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@DirtiesContext
@RunWith(SpringRunner.class)
@SpringBootTest(classes = CloudFoundryUsageMetricsSourceApplication.class)
public class CloudFoundryUsageMetricsSourceTest {

    private Log log = LogFactory.getLog(getClass());

    @Autowired
    private Source channels;

    @Autowired
    private MessageCollector messageCollector;

    @Autowired
    private CloudFoundryOperations cloudFoundryClient;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private CloudFoundryUsageMetricsSourceProperties properties;

    String urlFor(String appName) {
        return
                "http://" + (this.cloudFoundryClient
                        .applications()
                        .get(GetApplicationRequest.builder().name(appName).build())
                        .map(applicationDetail -> applicationDetail.getUrls().iterator().next())
                        .block());
    }

    @Autowired
    private Environment environment;

    @Autowired
    private CloudFoundryService cloudFoundryService;

    @Test
    public void testMetricsRead() throws Exception {
        String key = "cloudfoundry.metrics.source.application-name";
        String appName = this.properties.getApplicationName();
        if (!StringUtils.hasText(appName)) {
            this.log.warn("there is no " + key + " property specified. quitting test early.");
            return;
        }
        if (!cloudFoundryService.applicationExists(appName)) {
            this.log.warn("the specified application, '" + appName + "', does not exist. " +
                    "quitting test early.");
            return;
        }

        BlockingQueue<Message<?>> messageBlockingQueue = this.messageCollector
                .forChannel(this.channels.output());
        String uri = urlFor(appName);
        assertTrue(this.restTemplate.getForEntity(uri, String.class).getStatusCode()
                .is2xxSuccessful());

        Message<?> message = messageBlockingQueue.poll(1000 * 100, TimeUnit.MILLISECONDS);
        assertNotNull(message);
        assertTrue(Map.class.isAssignableFrom(message.getPayload().getClass()));
        Map payload = Map.class.cast(message.getPayload());
        assertTrue(payload.keySet().contains("MEM"));
        assertTrue(payload.keySet().contains("DISK"));
        log.info("received: " + message.toString());
    }

    @Configuration
    public static class RestTemplateConfiguration {

        @Bean
        public RestTemplate restTemplate() {
            return new RestTemplate();
        }
    }
}