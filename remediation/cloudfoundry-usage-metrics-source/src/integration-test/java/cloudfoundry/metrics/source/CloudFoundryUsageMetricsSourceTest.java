package cloudfoundry.metrics.source;

import cnj.CloudFoundryService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudfoundry.operations.CloudFoundryOperations;
import org.cloudfoundry.operations.applications.GetApplicationRequest;
import org.cloudfoundry.operations.applications.StartApplicationRequest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.messaging.Message;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
@Ignore
// TODO move this to the operations-it module
@DirtiesContext
@RunWith(SpringRunner.class)
@SpringBootTest(classes = CloudFoundryUsageMetricsSourceApplication.class)
public class CloudFoundryUsageMetricsSourceTest {

    private Log log = LogFactory.getLog(getClass());

    @Autowired
    private Source channels;

    @Autowired
    private MessageCollector messageCollector;

    @Autowired
    private CloudFoundryOperations ops;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private CloudFoundryUsageMetricsSourceProperties properties;

    private String applicationName;

    private File tempRootForStaging, manifestFile, jarFile;

    private void deploySampleApp() throws Throwable {
        Assert.assertTrue(tempRootForStaging.exists() || tempRootForStaging.mkdirs());
        Resource manifest = new ClassPathResource("/manifest.yml");
        Resource jar = new ClassPathResource("/hi.jar");
        try (InputStream mi = manifest.getInputStream();
             InputStream ji = jar.getInputStream();
             OutputStream mo = new FileOutputStream(manifestFile);
             OutputStream jo = new FileOutputStream(jarFile)) {
            StreamUtils.copy(mi, mo);
            StreamUtils.copy(ji, jo);
        }
        log.info("staging application at " + tempRootForStaging.getAbsolutePath());

        Assert.assertTrue(
                "the hi.jar should exist at " + this.jarFile.getAbsolutePath(),
                this.jarFile.exists());

        Assert.assertTrue(
                "the manifest.yml should exist at " + this.manifestFile.getAbsolutePath(),
                this.manifestFile.exists());

        this.applicationName = this.cloudFoundryService
                .applicationManifestFrom(this.manifestFile).entrySet().stream()
                .map(e -> e.getValue().getName()).findAny().orElse(null);

        log.info("this.applicationName: " + this.applicationName);

        this.cloudFoundryService.pushApplicationUsingManifest(this.manifestFile);
    }


    @Before
    public void before() throws Throwable {
        this.tempRootForStaging = Files.createTempDirectory("staging").toFile();

        this.manifestFile = new File(tempRootForStaging, "manifest.yml");
        this.jarFile = new File(tempRootForStaging, "hi.jar");

        log.info("manifest file will be copied to " + manifestFile.toString());
        log.info(".jar file will be copied to " + jarFile.toString());
        this.deploySampleApp();
    }

    private String urlFor(String appName) {
        return
                "http://" + (this.ops
                        .applications()
                        .get(GetApplicationRequest.builder().name(appName).build())
                        .map(applicationDetail -> applicationDetail.getUrls().iterator().next())
                        .block());
    }

    @Autowired
    private Environment environment;

    @Autowired
    private CloudFoundryService cloudFoundryService;

    @Test
    public void testMetricsRead() throws Exception {
        String key = "cloudfoundry.metrics.source.application-name";
        String appName = this.properties.getApplicationName();
        if (!StringUtils.hasText(appName)) {
            this.log.warn("there is no " + key + " property specified. quitting test early.");
            return;
        }
        if (!cloudFoundryService.applicationExists(appName)) {
            this.log.warn("the specified application, '" + appName + "', does not exist. " +
                    "quitting test early.");
            return;
        }

        BlockingQueue<Message<?>> messageBlockingQueue = this.messageCollector
                .forChannel(this.channels.output());
        String uri = urlFor(appName) + "/hi";
        log.info("uri: " + uri);
        assertTrue(this.restTemplate.getForEntity(uri, String.class).getStatusCode()
                .is2xxSuccessful());

        Message<?> message = messageBlockingQueue.poll(1000 * 100, TimeUnit.MILLISECONDS);
        assertNotNull(message);
        assertTrue(Map.class.isAssignableFrom(message.getPayload().getClass()));
        Map payload = Map.class.cast(message.getPayload());
        assertTrue(payload.keySet().contains("MEM"));
        assertTrue(payload.keySet().contains("DISK"));
        log.info("received: " + message.toString());
    }

    @Configuration
    public static class Config {

        @Bean
        public RestTemplate restTemplate() {
            return new RestTemplate();
        }
    }
}