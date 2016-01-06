package demo;


import com.sun.tools.javac.util.Assert;
import junit.framework.AssertionFailedError;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudfoundry.client.lib.CloudCredentials;
import org.cloudfoundry.client.lib.CloudFoundryClient;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestTemplate;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = CloudFoundryClientConfiguration.class)
public class OperationsIntegrationTest {

    @Autowired
    private CloudFoundryClient cloudFoundryClient;
    private Log log = LogFactory.getLog(getClass());
    private RestTemplate restTemplate = new RestTemplate();
    private String zcAName = "zipkin-client-a";
    private String zcBName = "zipkin-client-b";

    @Test
    public void trace() throws Exception {

        String client = this.urlForApp(zcAName),
                service = this.urlForApp(zcBName);

        ParameterizedTypeReference<Map<String, String>> ptr =
                new ParameterizedTypeReference<Map<String, String>>() {
                };

        ResponseEntity<Map<String, String>> serviceGreeting = this.restTemplate.exchange(service, HttpMethod.GET, null, ptr);
        serviceGreeting.getBody().entrySet().forEach(e -> this.log.info(e.getKey() + '=' + e.getValue()));
        assertFalse(serviceGreeting.getBody().containsKey("x-trace-id"));

        ResponseEntity<Map<String, String>> clientGreeting = this.restTemplate.exchange(client, HttpMethod.GET, null, ptr);
        clientGreeting.getBody().entrySet().forEach(e -> this.log.info(e.getKey() + '=' + e.getValue()));
        assertTrue(clientGreeting.getBody().containsKey("x-trace-id"));
    }

    private String urlForApp(String appName) {
        String url = this.cloudFoundryClient
                .getApplications()
                .stream()
                .filter(ca -> ca.getName().equals(appName))
                .map(ca -> ca.getUris().stream().findFirst())
                .findFirst()
                .orElseThrow(AssertionFailedError::new)
                .get();
        if (!url.toLowerCase().startsWith("http"))
            url = "http://" + url;
        return url;

    }
}

@SpringBootApplication
class CloudFoundryClientConfiguration {

    @Bean
    CloudCredentials cloudCredentials(
            @Value("${cf.user}") String email, @Value("${cf.password}") String pw) {
        return new CloudCredentials(email, pw);
    }

    @Bean
    CloudFoundryClient cloudFoundryClient(@Value("${cf.api}") String url,
                                          CloudCredentials cc) throws MalformedURLException {
        URI uri = URI.create(url);
        CloudFoundryClient cloudFoundryClient = new CloudFoundryClient(cc, uri.toURL());
        cloudFoundryClient.login();
        return cloudFoundryClient;
    }
}
