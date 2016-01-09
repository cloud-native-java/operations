package nurse.logging.cloudfoundry;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "nurse.logging.cloudfoundry")
class LoggregatorSourceProperties {

    private String applicationName;

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }
}
