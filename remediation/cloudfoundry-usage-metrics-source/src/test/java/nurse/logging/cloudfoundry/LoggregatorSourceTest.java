package nurse.logging.cloudfoundry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudfoundry.client.lib.CloudFoundryClient;
import org.cloudfoundry.client.lib.domain.CloudApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@DirtiesContext
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = CloudFoundryUsageMetricsSourceApplication.class)
public class LoggregatorSourceTest {

    private Log log = LogFactory.getLog(getClass());

    @Autowired
    private Source channels;

    @Autowired
    private MessageCollector messageCollector;

    @Autowired
    private CloudFoundryClient cloudFoundryClient;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private CloudFoundryUsageMetricsSourceProperties properties;

    @Configuration
    public static class RestTemplateConfiguration {

        @Bean
        public RestTemplate restTemplate() {
            return new RestTemplate();
        }
    }

    @Test
    public void testLogReceipt() throws Exception {
        BlockingQueue<Message<?>> messageBlockingQueue =
                this.messageCollector.forChannel(this.channels.output());

        String appName = this.properties.getApplicationName();
        CloudApplication cloudApplication = this.cloudFoundryClient.getApplication(appName);

        assertNotNull("cloudApplication is not null", cloudApplication);

        String uri = String.format("http://%s/project-name", cloudApplication.getUris().iterator().next());

        assertTrue(
                this.restTemplate.getForEntity(uri, String.class).getStatusCode().is2xxSuccessful());

        Message<?> message = messageBlockingQueue.poll(1000 * 100, TimeUnit.MILLISECONDS);
        assertNotNull(message);
        assertTrue(String.class.isAssignableFrom(message.getPayload().getClass()));
        log.info( "received: " +message.toString());
    }


}