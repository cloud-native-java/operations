package operations;

import cnj.CloudFoundryService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudfoundry.operations.CloudFoundryOperations;
import org.cloudfoundry.operations.applications.PushApplicationRequest;
import org.cloudfoundry.operations.applications.SetEnvironmentVariableApplicationRequest;
import org.cloudfoundry.operations.applications.StartApplicationRequest;
import org.cloudfoundry.operations.services.BindServiceInstanceRequest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.dataflow.rest.client.DataFlowTemplate;
import org.springframework.cloud.dataflow.rest.client.StreamOperations;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = RemediationIT.Config.class)
public class RemediationIT {

    private final Object monitor = new Object();

    private final Log log = LogFactory.getLog(getClass());

    private String demoRabbitMqServiceName = "remediation-rmq";

    private String baseCfDfAppName = "remediation-cfdf";

    private String rmqMetricsLogStreamName = "rmq-metrics-log";

    private DataFlowTemplate dataFlowTemplate;

    @Autowired
    private CloudFoundryOperations cloudFoundryOperations;

    private final RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private CloudFoundryService cloudFoundryService;

    private File remediationProducerManifest, remediationConsumerManifest, remediationAppDefinitionsManifest;

    @Before
    public void before() throws Throwable {
        File root = new File(".");
        this.remediationConsumerManifest = new File(root, "../remediation/remediation-rabbitmq-consumer/manifest.yml");
        this.remediationProducerManifest = new File(root, "../remediation/remediation-rabbitmq-producer/manifest.yml");
        this.remediationAppDefinitionsManifest = new File(root, "../remediation/remediation-app-definitions/manifest.yml");

        Stream.of(
                this.remediationAppDefinitionsManifest,
                this.remediationConsumerManifest,
                this.remediationProducerManifest)
                .forEach(m -> Assert.assertTrue(m.exists()));

        deployDemoPreRequisites();
        deployDemoConsumer();
        deployDemoProducer();
        startProduction();
        deployRemediationAppDefinitions();
        deployDataFlowServer();
        deployAppDefinitionsToDataFlowServer();
        deployRemediationStream();
    }

    private void startProduction() {
        String url = cloudFoundryService.urlForApplication(this.applicationNameFromManifest(
                this.remediationProducerManifest));
        this.restTemplate.getForEntity(url + "/start", String.class);
    }

    private String cf() {
        StringBuilder res = new StringBuilder();
        System.getenv().forEach((k, v) -> {
            if (k.toLowerCase().contains("cf_")) {
                res.append(" --")
                        .append(k.toLowerCase().replace("_", "."))
                        .append('=')
                        .append(v);
            }
        });
        return res.toString();
    }

    private boolean dataFlowDefinitionsNeedsCleaning() {
        DataFlowTemplate dataFlowTemplate = this.lazyDataFlowTemplate();
        StreamOperations streamOperations = dataFlowTemplate.streamOperations();
        return streamOperations.list().getContent()
                .stream()
                .anyMatch(sdr -> sdr.getName().equalsIgnoreCase(this.rmqMetricsLogStreamName));
    }

    private void deployRemediationStream() {


        String rabbitAddressProperty = " --spring.rabbitmq.addresses=${vcap.services." + this.demoRabbitMqServiceName + ".credentials.uri}";

        String streamDefinition = "rabbit-queue-metrics --rabbitmq.metrics.queueName=remediation-demo.remediation-demo-group  " + rabbitAddressProperty +
                "| transform --expression=headers['queue-size'] " +
                "| cloudfoundry-autoscaler " + cf() +
                " --cloudfoundry.autoscaler.sink.instanceCountMinimum=1 " +
                " --cloudfoundry.autoscaler.sink.applicationName=remediation-rabbitmq-consumer " +
                " --cloudfoundry.autoscaler.sink.instanceCountMaximum=10 " +
                " --cloudfoundry.autoscaler.sink.thresholdMaximum=5 ";


      /*  String streamDefinition = "rabbit-queue-metrics --management.security.enabled=false --spring.rabbitmq.addresses=${vcap.services." +
                this.demoRabbitMqServiceName + ".credentials.uri} --rabbitmq.metrics.queueName=remediation-demo.remediation-demo-group " +
                // "| transform --expression=payload['consumers'] " +
                "| log --expression=headers['queue-consumers']  ";
      */
        log.info("stream definition: " + streamDefinition);
        StreamOperations streamOperations = lazyDataFlowTemplate().streamOperations();
        if (this.dataFlowDefinitionsNeedsCleaning()) {
            log.info("calling destroyAll()");
            streamOperations.destroyAll();
            log.info("there are " + streamOperations.list().getContent().size() + " streams now.");
        }
        streamOperations.createStream(this.rmqMetricsLogStreamName, streamDefinition, false);
        streamOperations.deploy(this.rmqMetricsLogStreamName,
                Collections.singletonMap("deployer.rabbit-queue-metrics.cloudfoundry.services", this.demoRabbitMqServiceName));
        log.info("deployed stream " + this.rmqMetricsLogStreamName);
    }

    private boolean appExists(File f) {
        return cloudFoundryService.applicationExists(
                applicationNameFromManifest(f));
    }

    private void pushIfDoesNotExist(File f) {
        if (!appExists(f)) {
            this.cloudFoundryService.pushApplicationUsingManifest(f);
        }
    }

    private URI applicationDefinitionPropertiesURI() {
        String appName = applicationNameFromManifest(this.remediationAppDefinitionsManifest);
        String urlForAppDefinitions =
                this.cloudFoundryService.urlForApplication(appName);
        if (!urlForAppDefinitions.endsWith("/")) {
            urlForAppDefinitions = urlForAppDefinitions + "/";
        }
        return URI.create(urlForAppDefinitions + "remediation-apps.properties");
    }


    private void deployRemediationAppDefinitions() {
        pushIfDoesNotExist(remediationAppDefinitionsManifest);
    }

    private void deployDemoPreRequisites() {
        cloudFoundryService.createServiceIfMissing(
                "cloudamqp", "tiger", demoRabbitMqServiceName);
    }

    private String applicationNameFromManifest(File file) {
        return cloudFoundryService
                .applicationManifestFrom(file)
                .entrySet()
                .stream()
                .findFirst()
                .map(e -> e.getValue().getName())
                .orElse(null);
    }

    private void deployDemoProducer() {
        pushIfDoesNotExist(this.remediationProducerManifest);
    }

    private void deployDemoConsumer() {
        pushIfDoesNotExist(this.remediationConsumerManifest);
    }


    private DataFlowTemplate lazyDataFlowTemplate() {
        synchronized (this.monitor) {
            if (null == this.dataFlowTemplate) {
                this.dataFlowTemplate = dataFlowTemplate(this.baseCfDfAppName);
                this.log.info("created new " + DataFlowTemplate.class.getName());
            }
            return this.dataFlowTemplate;
        }
    }

    private void deployAppDefinitionsToDataFlowServer() {


        List<String> apps = new ArrayList<>();
        apps.add("http://repo.spring.io/libs-release-local/org/springframework/cloud/task/app/spring-cloud-task-app-descriptor/Addison.RELEASE/spring-cloud-task-app-descriptor-Addison.RELEASE.task-apps-maven");
        apps.add("http://repo.spring.io/libs-release/org/springframework/cloud/stream/app/spring-cloud-stream-app-descriptor/Avogadro.SR1/spring-cloud-stream-app-descriptor-Avogadro.SR1.stream-apps-rabbit-maven");
        apps.add(this.applicationDefinitionPropertiesURI().toString());
        apps
                .parallelStream()
                .forEach(s -> this.lazyDataFlowTemplate()
                        .appRegistryOperations().importFromResource(s, true));
    }

    @Test
    public void test() {

        // deploy a SCS producer and a consumer
        // deploy DF app definitions
        // deploy DF itself
        // deploy stream definition
        // start making requests of the producer and confirm that eventually autoscaler creates a second AI

        produceValuesAndTriggerAnAutoscale();

    }

    private void produceValuesAndTriggerAnAutoscale() {
        // todo
    }


    private DataFlowTemplate dataFlowTemplate(String cfDfServerName) {
        String urlForApplication = this.cloudFoundryService.urlForApplication(cfDfServerName);
        log.info("attempting to create a DataFlowTemplate using the following API endpoint " + urlForApplication);
        return new DataFlowTemplate(URI.create(urlForApplication),
                new RestTemplate());
    }

    private String serverJarUrl() {
        String url = "http://repo.spring.io/libs-snapshot/" +
                "org/springframework/cloud/spring-cloud-dataflow-server-cloudfoundry/1.2.0.RC1/spring-cloud-dataflow-server-cloudfoundry-1.2.0.RC1.jar";
        log.info("server .jar URL: " + url);
        return url;
    }

    private void deployDataFlowServer() throws Throwable {

        log.info("deploying the Spring Cloud Data Flow Cloud Foundry Server.");

        if (this.cloudFoundryService.applicationExists(this.baseCfDfAppName)) {
            return;
        }

        // deploy the DF server
        String serverRedis = baseCfDfAppName + "-redis",
                serverMysql = baseCfDfAppName + "-mysql",
                serverRabbit = baseCfDfAppName + "-rabbit";
        Stream.of("rediscloud 100mb " + serverRedis, "cloudamqp lemur " + serverRabbit, "p-mysql 100mb " + serverMysql)
                .parallel()
                .map(x -> x.split(" "))
                .forEach(tpl -> this.cloudFoundryService.createServiceIfMissing(tpl[0], tpl[1], tpl[2]));

        String urlForServerJarDistribution = this.serverJarUrl();
        File cfdfJar = new File(System.getProperty("user.home"),
                this.baseCfDfAppName + ".jar");
        Assert.assertTrue(cfdfJar.getParentFile().exists() || cfdfJar.getParentFile().mkdirs());
        Path targetFile = cfdfJar.toPath();
        if (!cfdfJar.exists()) {
            URI uri = URI.create(urlForServerJarDistribution);
            try (InputStream inputStream = uri.toURL().openStream()) {
                java.nio.file.Files.copy(inputStream, targetFile,
                        StandardCopyOption.REPLACE_EXISTING);
            }
            this.log.info("..downloaded Data Flow server .jar to "
                    + targetFile.toFile().getAbsolutePath() + ".");
        }
        log.info("Data Flow server .jar lives at " + cfdfJar.getAbsolutePath());

        int twoG = 1024 * 2;
        this.cloudFoundryOperations
                .applications()
                .push(
                        PushApplicationRequest
                                .builder()
                                .application(targetFile)
                                .buildpack("https://github.com/cloudfoundry/java-buildpack.git")
                                .noStart(true)
                                .name(this.baseCfDfAppName)
                                .host("cfdf-" + UUID.randomUUID().toString())
                                .memory(twoG)
                                .diskQuota(twoG).build())
                .block();
        log.info("..pushed (but didn't start) the Data Flow server");

        Map<String, String> env = new ConcurrentHashMap<>();

        // CF authentication
        env.put("SPRING_CLOUD_DEPLOYER_CLOUDFOUNDRY_ORG", System.getenv("CF_ORG"));
        env
                .put("SPRING_CLOUD_DEPLOYER_CLOUDFOUNDRY_SPACE", System.getenv("CF_SPACE"));
        env.put("SPRING_CLOUD_DEPLOYER_CLOUDFOUNDRY_USERNAME",
                System.getenv("CF_USER"));
        env.put("SPRING_CLOUD_DEPLOYER_CLOUDFOUNDRY_PASSWORD",
                System.getenv("CF_PASSWORD"));
        env.put("SPRING_CLOUD_DEPLOYER_CLOUDFOUNDRY_TASK_API_TIMEOUT", "120");

        env.put("SPRING_CLOUD_DEPLOYER_CLOUDFOUNDRY_STREAM_SERVICES", serverRabbit);
        env.put("SPRING_CLOUD_DEPLOYER_CLOUDFOUNDRY_TASK_SERVICES", serverMysql);

        env.put("SPRING_CLOUD_DEPLOYER_CLOUDFOUNDRY_SKIP_SSL_VALIDATION", "false");
        env.put("SPRING_CLOUD_DEPLOYER_CLOUDFOUNDRY_URL",
                "https://api.run.pivotal.io");
        env.put("SPRING_CLOUD_DEPLOYER_CLOUDFOUNDRY_DOMAIN", "cfapps.io");
        env.put("MAVEN_REMOTE_REPOSITORIES_LR_URL",
                "https://cloudnativejava.artifactoryonline.com/cloudnativejava/libs-release");
        env.put("MAVEN_REMOTE_REPOSITORIES_LS_URL",
                "https://cloudnativejava.artifactoryonline.com/cloudnativejava/libs-snapshot");
        env.put("MAVEN_REMOTE_REPOSITORIES_PR_URL",
                "https://cloudnativejava.artifactoryonline.com/cloudnativejava/plugins-release");
        env.put("MAVEN_REMOTE_REPOSITORIES_PS_URL",
                "https://cloudnativejava.artifactoryonline.com/cloudnativejava/plugins-snapshot");

        env.put("SPRING_CLOUD_DEPLOYER_CLOUDFOUNDRY_STREAM_INSTANCES", "1");

        env.forEach((k, v) -> {
            this.cloudFoundryOperations
                    .applications()
                    .setEnvironmentVariable(
                            SetEnvironmentVariableApplicationRequest.builder().name(baseCfDfAppName)
                                    .variableName(k).variableValue(v).build()).block();

            log.info("..set environment variable for " + baseCfDfAppName + ": " + k);
        });

        log.info("..set all " + env.size() + " environment variables.");

        // bind the relevant services to DF
        Stream.of(serverMysql, serverRedis).forEach(
                svc -> {
                    log.info("..binding " + svc + " to " + this.baseCfDfAppName);
                    this.cloudFoundryOperations
                            .services()
                            .bind(BindServiceInstanceRequest.builder().applicationName(baseCfDfAppName)
                                    .serviceInstanceName(svc).build()).block();
                    log.info("..binding " + svc + " to " + baseCfDfAppName);
                });

        // start
        this.cloudFoundryOperations
                .applications()
                .start(
                        StartApplicationRequest.builder().stagingTimeout(Duration.ofMinutes(10))
                                .startupTimeout(Duration.ofMinutes(10)).name(baseCfDfAppName).build()).block();

        log.info("started the Spring Cloud Data Flow Cloud Foundry server.");
    }

    @EnableAutoConfiguration
    @Configuration
    public static class Config {
    }
}
