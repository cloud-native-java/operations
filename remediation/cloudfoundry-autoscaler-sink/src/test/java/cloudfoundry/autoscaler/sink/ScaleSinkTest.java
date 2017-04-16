package cloudfoundry.autoscaler.sink;

import org.cloudfoundry.operations.CloudFoundryOperations;
import org.cloudfoundry.operations.applications.GetApplicationRequest;
import org.cloudfoundry.operations.applications.ScaleApplicationRequest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ScaleSinkTest.Application.class)
public class ScaleSinkTest {

    private static int MAX = 90;
    private static int MIN = 30;

    private static int MAX_AIS = 7;

    private RetryTemplate retryTemplate = new RetryTemplate();

    @Autowired
    private Sink sink;

    @Autowired
    private CloudFoundryOperations ops;

    @Autowired
    private AutoScalerSinkProperties props;

    @Before
    public void before() throws Throwable {
        scale(this.props.getApplicationName(), 1);
    }

    @Test
    public void upAndDown() throws Exception {

        // what we're testing:
        // this simulates a metric publishing
        // fictional CPU %.
        // we'll send that we're at just 1% over the
        // threshold
        // and once we've confirmed that the
        // auto-scaler has started 5
        // instances trying to lower the instance
        // count, we'll let it off the hook by publishing a metric
        // 1% below the threshold.

        // this should publish stepping messages,
        // stepping instances counts from 1->2, 2->3,
        // 3->4

        int desiredInstanceCount = 4;
        while (instances() < desiredInstanceCount) {
            this.sink.input().send(MessageBuilder.withPayload(MAX + 1).build());
        }
        Assert.assertEquals(instances(), desiredInstanceCount);

        // this should publish stepping messages,
        // stepping instances counts from 4->3, 3->2, 2->1
        desiredInstanceCount = 1;
        while (instances() > desiredInstanceCount) {
            this.sink.input().send(MessageBuilder.withPayload(MIN - 1).build());
        }
        Assert.assertEquals(instances(), desiredInstanceCount);
    }

    @Test
    public void upAndDownWithinBounds() throws Exception {
        SubscribableChannel input = this.sink.input();
        int start = MAX_AIS - 2;

        scale(this.props.getApplicationName(), start);
        Assert.assertEquals(start, instances());

        input.send(MessageBuilder.withPayload(MAX + 1).build());
        Assert.assertEquals(start + 1, instances());

        input.send(MessageBuilder.withPayload(MAX + 1).build());
        Assert.assertEquals(start + 2, instances());

        // we know that this should kick off another
        // step raise but we've capped it so it
        // shouldn't go any further
        input.send(MessageBuilder.withPayload(MAX + 1).build());
        Assert.assertEquals(start + 2, instances());

    }

    private int instances() {
        String appName = this.props.getApplicationName();
        return this.ops.applications()
                .get(GetApplicationRequest.builder()
                        .name(appName)
                        .build())
                .block()
                .getInstances();
    }

    private void scale(String appName, int count) {
        retryTemplate.execute(ctx -> {
            this.ops.applications()
                    .scale(ScaleApplicationRequest
                            .builder()
                            .name(appName)
                            .instances(count)
                            .build())
                    .block();
            return null;
        });
    }

    @SpringBootApplication
    public static class Application {
    }
}
