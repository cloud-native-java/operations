package nurse.logging.cloudfoundry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudfoundry.client.lib.ApplicationLogListener;
import org.cloudfoundry.client.lib.CloudFoundryClient;
import org.cloudfoundry.client.lib.domain.ApplicationLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.cloud.stream.module.PeriodicTriggerConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.integration.endpoint.MessageProducerSupport;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;

import java.util.HashMap;
import java.util.Map;

@EnableBinding(Source.class)
@Import(PeriodicTriggerConfiguration.class)
@EnableConfigurationProperties(LoggregatorSourceProperties.class)
public class LoggregatorSource {

    @Autowired
    private LoggregatorSourceProperties loggregatorSourceProperties;

    @Bean
    LoggregatorMessageSource loggregatorMessageSource(
            @Qualifier(Source.OUTPUT) MessageChannel source,
            CloudFoundryClient cloudFoundryClient) {
        return new LoggregatorMessageSource(
                this.loggregatorSourceProperties.getApplicationName(),
                cloudFoundryClient, source);
    }
}

class LoggregatorMessageSource extends MessageProducerSupport {

    private final CloudFoundryClient cloudFoundryClient;
    private final String applicationName;

    protected LoggregatorMessageSource(
            String applicationName,
            CloudFoundryClient cloudFoundryClient,
            MessageChannel out) {
        super();
        this.applicationName = applicationName;
        this.cloudFoundryClient = cloudFoundryClient;
        setOutputChannel(out);
    }

    @Override
    protected void doStart() {
        this.cloudFoundryClient.streamLogs(this.applicationName,
                new LoggregatorApplicationLogListener());
    }

    private class LoggregatorApplicationLogListener implements ApplicationLogListener {

        private Log log = LogFactory.getLog(getClass());

        @Override
        public void onMessage(ApplicationLog applicationLog) {
            Map<String, Object> headers = new HashMap<>();
            headers.put(LoggregatorHeaders.APPLICATION_ID.toString(), applicationLog.getAppId());
            headers.put(LoggregatorHeaders.SOURCE_ID.toString(), applicationLog.getSourceId());
            headers.put(LoggregatorHeaders.MESSAGE_TYPE.toString(), applicationLog.getMessageType().toString());
            headers.put(LoggregatorHeaders.SOURCE_NAME.toString(), applicationLog.getSourceName());
            headers.put(LoggregatorHeaders.TIMESTAMP.toString(), applicationLog.getTimestamp());
            sendMessage(MessageBuilder.withPayload(applicationLog.getMessage())
                    .copyHeaders(headers)
                    .build());
        }

        @Override
        public void onComplete() {
            log.info(String.format("completed streaming logs @ %s", getClass().getName()));
        }

        @Override
        public void onError(Throwable throwable) {
            log.error(String.format("error when streaming logs from %s in %s",
                    applicationName, getClass().getName()), throwable);
        }
    }

    public enum LoggregatorHeaders {
        APPLICATION_ID,
        MESSAGE_TYPE,
        SOURCE_ID,
        SOURCE_NAME,
        TIMESTAMP
    }
}