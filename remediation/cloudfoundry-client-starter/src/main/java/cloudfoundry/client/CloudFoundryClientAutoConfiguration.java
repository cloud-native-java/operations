package cloudfoundry.client;

import org.cloudfoundry.client.lib.CloudCredentials;
import org.cloudfoundry.client.lib.CloudFoundryClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.MalformedURLException;
import java.net.URI;

/**
 * Auto-configures basic connectivity to the
 * <a href="http://cloudfoundry.org">CloudFoundry</a>
 * API letting clients programmatically do things like
 * talk to the loggregator, scale instances, and ask
 * questions about the state of deployed applications
 * and their instances.
 *
 * @author <a href="mailto:josh@joshlong.com">Josh Long</a>
 */
@Configuration
@ConditionalOnClass({CloudFoundryClient.class})
@EnableConfigurationProperties(CloudFoundryClientProperties.class)
public class CloudFoundryClientAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean(CloudCredentials.class)
	public CloudCredentials cloudCredentials(CloudFoundryClientProperties clientProperties) {
		return new CloudCredentials(clientProperties.getUsername(), clientProperties.getPassword());
	}

	@Bean(initMethod = "login")
	@ConditionalOnMissingBean(CloudFoundryClient.class)
	public CloudFoundryClient cloudFoundryClient(
			CloudCredentials cc, CloudFoundryClientProperties clientProperties) throws MalformedURLException {
		URI uri = URI.create(clientProperties.getApiEndpoint());
		CloudFoundryClient cloudFoundryClient = new CloudFoundryClient(cc, uri.toURL());
		return cloudFoundryClient;
	}

}
