package nurse.scaler.cloudfoundry;

import org.cloudfoundry.client.lib.CloudFoundryClient;
import org.cloudfoundry.client.lib.domain.CloudApplication;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 *
 * @author Josh Long
 */
public class AutoScalerMessageHandler implements MessageHandler {

    private final Number maxNumber, minNumber;
    private final String applicationName, metricHeaderKey;
    private final CloudFoundryClient client;

    public AutoScalerMessageHandler(
            CloudFoundryClient client,
            Number minNumber,
            Number maxNumber,
            String metricHeaderKey,
            String appName) {

        this.client = client;
        this.maxNumber = maxNumber;
        this.minNumber = minNumber;
        this.applicationName = appName;
        this.metricHeaderKey = metricHeaderKey;
    }

    @Override
    public void handleMessage(Message<?> message) throws MessagingException {
        String key = this.metricHeaderKey;
        Object incomingValue = (StringUtils.hasText(key) && message.getHeaders().containsKey(key)) ?
                message.getHeaders().get(key) :
                message.getPayload();

        Assert.isTrue(incomingValue.getClass().isAssignableFrom(Number.class));
        Number incomingNumber = Number.class.cast(incomingValue);

        // two simple use cases
        double v = incomingNumber.doubleValue(); // lets say this is CPU% and its right now at 90%
        double max = maxNumber.doubleValue(); // max acceptable rate is 80%, which means we need to scale up
        double min = minNumber.doubleValue();

        // if the CPU is 90% and the max tolerable threshold is 70%, then we need to add more capacity
        if (v > max) {
            scale(this.applicationName, 1);
        }

        // if the CPU is 10% & the min tolerable threashold is 20%, then we have too much capacity, scale down
        if (v < min) {
            scale(this.applicationName, -1);
        }
    }

    protected void scale(String appName, int delta) throws MessagingException {
        CloudApplication application = client.getApplication(appName);
        int currentInstances = application.getInstances();
        int newSum = currentInstances + delta;
        if (!(newSum <= 0)) {
            client.updateApplicationInstances(appName, newSum);
        }
    }
}