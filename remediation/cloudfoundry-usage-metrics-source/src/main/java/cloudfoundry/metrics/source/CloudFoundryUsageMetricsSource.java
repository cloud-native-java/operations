package cloudfoundry.metrics.source;

import org.cloudfoundry.operations.CloudFoundryOperations;
import org.cloudfoundry.operations.applications.ApplicationDetail;
import org.cloudfoundry.operations.applications.GetApplicationRequest;
import org.cloudfoundry.operations.applications.InstanceDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.app.trigger.TriggerConfiguration;
import org.springframework.cloud.stream.app.trigger.TriggerPropertiesMaxMessagesDefaultUnlimited;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.core.Pollers;
import org.springframework.integration.scheduling.PollerMetadata;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.Trigger;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
@EnableBinding(Source.class)
@Import(TriggerConfiguration.class)
@EnableConfigurationProperties({
 CloudFoundryUsageMetricsSourceProperties.class,
 TriggerPropertiesMaxMessagesDefaultUnlimited.class })
public class CloudFoundryUsageMetricsSource {

 @Autowired
 private Source source;

 @Autowired
 private Trigger trigger;

 @Bean
 public PollerMetadata poller() {
  return Pollers.trigger(trigger).get();
 }

 @Bean
 public CloudFoundryUsageMetricsMessageSource cloudFoundryUsageMetricsMessageSource(
  CloudFoundryUsageMetricsSourceProperties properties,
  CloudFoundryOperations client) {
  return new CloudFoundryUsageMetricsMessageSource(
   properties.getApplicationName(), client);
 }

 @Bean
 public IntegrationFlow cloudFoundryUsageMetricsSourceFlow(
  CloudFoundryUsageMetricsMessageSource msgSrc) {
  return IntegrationFlows
   .from(msgSrc, pollerSpec -> pollerSpec.poller(poller()))
   .channel(this.source.output()).get();
 }
}

/**
 * calculates the average of key metrics
 * in Cloud Foundry like application
 * disk-usage and memory. You can funnel
 * these metrics into something
 * interesting like a remediation flow.
 */
class CloudFoundryUsageMetricsMessageSource implements
 MessageSource<Map<String, Double>> {

 private final String applicationName;

 private final CloudFoundryOperations cloudFoundryClient;

 CloudFoundryUsageMetricsMessageSource(String applicationName,
  CloudFoundryOperations cloudFoundryClient) {
  this.applicationName = applicationName;
  this.cloudFoundryClient = cloudFoundryClient;
 }

 @Override
 public Message<Map<String, Double>> receive() {
  Mono<ApplicationDetail> applicationDetailMono = this.cloudFoundryClient
   .applications().get(
    GetApplicationRequest.builder().name(applicationName).build());
  Flux<InstanceDetail> instanceDetailFlux = applicationDetailMono
   .flatMapIterable(ApplicationDetail::getInstanceDetails);
  Flux<Map<String, Double>> collect = instanceDetailFlux
   .map(this::instanceStatsMapFrom);
  Map<String, Double> avgs = new HashMap<>();
  avg(collect.collectList().block(), avgs, UsageHeaders.DISK);
  avg(collect.collectList().block(), avgs, UsageHeaders.MEM);
  return MessageBuilder.withPayload(avgs).build();
 }

 private Map<String, Double> instanceStatsMapFrom(InstanceDetail i) {
  Map<String, Double> m = new HashMap<>();
  Double disk = i.getDiskUsage().doubleValue();
  Double mem = i.getMemoryUsage().doubleValue();
  m.put(UsageHeaders.DISK.toString(), disk);
  m.put(UsageHeaders.MEM.toString(), mem);
  return m;
 }

 private void avg(List<Map<String, Double>> collection,
  Map<String, Double> avgs, UsageHeaders h) {
  String key = h.toString();
  Double avgDouble = collection.stream().map(m -> m.get(key))
   .collect(Collectors.averagingDouble(a -> a));
  avgs.put(key, avgDouble);
 }

 enum UsageHeaders {
  CPU, DISK, MEM
 }
}
