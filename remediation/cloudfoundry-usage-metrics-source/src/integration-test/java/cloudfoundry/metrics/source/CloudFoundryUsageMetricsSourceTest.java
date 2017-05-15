package cloudfoundry.metrics.source;

import cnj.CloudFoundryService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudfoundry.operations.CloudFoundryOperations;
import org.cloudfoundry.operations.applications.GetApplicationRequest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.EnvironmentTestUtils;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.io.FileSystemResource;
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

/*
@DirtiesContext
@RunWith(SpringRunner.class)
@SpringBootTest(classes = CloudFoundryUsageMetricsSourceApplication.class)
*/
@Ignore
public class CloudFoundryUsageMetricsSourceTest {

    private Log log = LogFactory.getLog(getClass());

    //    @Autowired
    private Source channels;

    //    @Autowired
    private MessageCollector messageCollector;

    //    @Autowired
    private CloudFoundryOperations ops;

    private ApplicationContext context = null;

    //    @Autowired
    private RestTemplate restTemplate;

    //    @Autowired
    private CloudFoundryUsageMetricsSourceProperties properties;

    private String applicationName;

    private File tempRootForStaging, manifestFile, jarFile;

    @Before
    public void before() throws Throwable {

        this.tempRootForStaging = Files.createTempDirectory("staging").toFile();

        this.manifestFile = new File(tempRootForStaging, "manifest.yml");
        this.jarFile = new File(tempRootForStaging, "hi.jar");

        log.info("manifest file will be copied to " + manifestFile.toString());
        log.info(".jar file will be copied to " + jarFile.toString());
        Assert.assertTrue(this.tempRootForStaging.exists() ||
                this.tempRootForStaging.mkdirs());
        File f = new File(".");
        log.info(f.getAbsolutePath());
        Resource manifest = new FileSystemResource(new File(f.getAbsoluteFile(), "src/integration-test/resources/manifest.yml"));
        Resource jar = new FileSystemResource(new File(f.getAbsoluteFile(),
                "src/integration-test/resources/hi.jar"));
        log.info(jar.getFile().toString());
        Assert.assertTrue(jar.exists());
        Assert.assertTrue(manifest.exists());
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


        this.context = SpringApplication.run(Config.class, "cloudfoundry.metrics.source.applicationName=cf-ums-demo-app");
        this.channels = context.getBean(Source.class);
        this.properties = context.getBean(CloudFoundryUsageMetricsSourceProperties.class);
        this.restTemplate = context.getBean(RestTemplate.class);
        this.messageCollector = context.getBean(MessageCollector.class);
        this.ops = context.getBean(CloudFoundryOperations.class);
    }


    public static class Initializer {

        private String applicationName;
        private File manifestFile ;

        Initializer (File manifestFile ,CloudFoundryService cloudFoundryService ){

            this.manifestFile = manifestFile ;
            this.applicationName =  cloudFoundryService
                    .applicationManifestFrom(this.manifestFile).entrySet().stream()
                    .map(e -> e.getValue().getName()).findAny().orElse(null);

            cloudFoundryService.pushApplicationUsingManifest(this.manifestFile);

        }
    }

    private String urlFor(String appName) {
        return "http://"
                + (this.ops.applications()
                .get(GetApplicationRequest.builder().name(appName).build())
                .map(applicationDetail -> applicationDetail.getUrls().iterator().next())
                .block());
    }

    @Autowired
    private CloudFoundryService cloudFoundryService;

    @Test
    public void testMetricsRead() throws Exception {
        String key = "cloudfoundry.metrics.source.application-name";
        String appName = this.properties.getApplicationName();
        if (!StringUtils.hasText(appName)) {
            this.log.warn("there is no " + key
                    + " property specified. quitting test early.");
            return;
        }
        if (!cloudFoundryService.applicationExists(appName)) {
            this.log.warn("the specified application, '" + appName
                    + "', does not exist. " + "quitting test early.");
            return;
        }

        BlockingQueue<Message<?>> messageBlockingQueue = this.messageCollector
                .forChannel(this.channels.output());
        String uri = urlFor(appName) + "/hi";
        log.info("uri: " + uri);
        assertTrue(this.restTemplate.getForEntity(uri, String.class).getStatusCode()
                .is2xxSuccessful());

        Message<?> message = messageBlockingQueue.poll(1000 * 100,
                TimeUnit.MILLISECONDS);
        assertNotNull(message);
        assertTrue(Map.class.isAssignableFrom(message.getPayload().getClass()));
        Map payload = Map.class.cast(message.getPayload());
        assertTrue(payload.keySet().contains("MEM"));
        assertTrue(payload.keySet().contains("DISK"));
        log.info("received: " + message.toString());
    }

    @Import(CloudFoundryUsageMetricsSourceApplication.class)
    @Configuration
    public static class Config {

        @Bean
        public RestTemplate restTemplate() {
            return new RestTemplate();
        }
    }
}