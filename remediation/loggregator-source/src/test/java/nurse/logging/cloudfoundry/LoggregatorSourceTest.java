package nurse.logging.cloudfoundry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.messaging.Message;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

@DirtiesContext
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = LoggregatorSourceApplication.class)
public class LoggregatorSourceTest {

    @Autowired
    private Source channels;

    @Autowired
    private MessageCollector messageCollector;

    private Log log = LogFactory.getLog(getClass());

    @Test
    public void testLogReceipt() throws Exception {
        BlockingQueue<Message<?>> messageBlockingQueue =
                this.messageCollector.forChannel(this.channels.output());
        Message<?> message;
        while ((message = messageBlockingQueue.poll(1000 * 100, TimeUnit.MILLISECONDS)) != null)
            log.info(message.toString());

    }
}