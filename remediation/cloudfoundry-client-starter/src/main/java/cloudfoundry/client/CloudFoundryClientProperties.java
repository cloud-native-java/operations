package cloudfoundry.client;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author <a href="mailto:josh@joshlong.com">Josh
 * Long</a>
 */
@ConfigurationProperties(prefix = "cloudfoundry.client")
public class CloudFoundryClientProperties {

	private String apiEndpoint = "http://api.run.pivotal.io"; // naturally.
	private String organization;
	private String space;
	private String username;
	private String password;

	public String getApiEndpoint() {
		return apiEndpoint;
	}

	public void setApiEndpoint(String apiEndpoint) {
		this.apiEndpoint = apiEndpoint;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getOrganization() {
		return organization;
	}

	public void setOrganization(String organization) {
		this.organization = organization;
	}

	public String getSpace() {
		return space;
	}

	public void setSpace(String space) {
		this.space = space;
	}
}
