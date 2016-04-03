package cloudfoundry.client;

import org.cloudfoundry.client.lib.CloudCredentials;
import org.cloudfoundry.client.lib.CloudFoundryClient;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertNotNull;

/**
 * @author <a href="mailto:josh@joshlong.com">Josh Long</a>
 */
public class CloudFoundryClientAutoConfigurationTest {

    private Map<String, Object> properties;

    @SpringBootApplication
    public static class SimpleBootApp {
    }

    @Before
    public void before() throws Exception {

        String prefix = "cloudfoundry.client";

        this.properties = new HashMap<>();
        this.properties.put(prefix + ".apiEndpoint", System.getenv("CF_API"));
        this.properties.put(prefix + ".username", System.getenv("CF_USER"));
        this.properties.put(prefix + ".password", System.getenv("CF_PASSWORD"));
    }

    @Test
    public void testAutoConfiguration() throws Throwable {

        ApplicationContext run = new SpringApplicationBuilder()
                .properties(this.properties)
                .sources(SimpleBootApp.class)
                .run();

        CloudFoundryClient client = run.getBean(CloudFoundryClient.class);
        CloudCredentials credentials = run.getBean(CloudCredentials.class);
        this.validate(credentials, client);
    }

    @Test
    public void testContextLoads() throws Throwable {

        MapPropertySource mapPropertySource = new MapPropertySource("test", this.properties);

        AnnotationConfigApplicationContext run = new AnnotationConfigApplicationContext();

        ConfigurableEnvironment environment = run.getEnvironment();
        environment.getPropertySources().addLast(mapPropertySource);

        run.register(CloudFoundryClientAutoConfiguration.class);
        run.refresh();

        CloudFoundryClient client = run.getBean(CloudFoundryClient.class);
        CloudCredentials credentials = run.getBean(CloudCredentials.class);
        this.validate(credentials, client);
    }

    private void validate(CloudCredentials cc, CloudFoundryClient client) throws Throwable {
        assertNotNull("no definition for " + CloudFoundryClient.class.getName() + ".", client);
        assertNotNull("no definition for " + CloudCredentials.class.getName() + ".", cc);
    }
}