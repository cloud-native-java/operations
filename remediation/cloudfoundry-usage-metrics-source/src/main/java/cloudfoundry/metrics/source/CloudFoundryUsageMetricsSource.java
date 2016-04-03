package cloudfoundry.metrics.source;

import org.cloudfoundry.client.lib.CloudFoundryClient;
import org.cloudfoundry.client.lib.domain.InstanceStats;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.cloud.stream.module.trigger.TriggerConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.core.Pollers;
import org.springframework.integration.scheduling.PollerMetadata;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.Trigger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This metrics source emits averages of a given Cloud Foundry
 * application's aggregate resource (CPU, memory, disk) usage.
 *
 * @author <a href="http://josh@joshlong.com">Josh Long</a>
 */
@EnableBinding(Source.class)
@Import(TriggerConfiguration.class)
@EnableConfigurationProperties(CloudFoundryUsageMetricsSourceProperties.class)
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
			CloudFoundryUsageMetricsSourceProperties properties, CloudFoundryClient client) {
		return new CloudFoundryUsageMetricsMessageSource(properties.getApplicationName(), client);
	}

	@Bean
	public IntegrationFlow cloudFoundryUsageMetricsSourceFlow(CloudFoundryUsageMetricsMessageSource msgSrc) {
		return IntegrationFlows
				.from(msgSrc, pollerSpec -> pollerSpec.poller(poller()))
				.channel(this.source.output())
				.get();
	}
}

/**
 * calculates the average of key metrics in Cloud Foundry like application
 * CPU, disk-usage, and memory. You can funnel these metrics into something interesting
 * like a remediation flow.
 */
class CloudFoundryUsageMetricsMessageSource
		implements MessageSource<Map<String, Double>> {

	private final String applicationName;
	private final CloudFoundryClient cloudFoundryClient;

	public CloudFoundryUsageMetricsMessageSource(String applicationName,
	                                             CloudFoundryClient cloudFoundryClient) {
		this.applicationName = applicationName;
		this.cloudFoundryClient = cloudFoundryClient;
	}

	@Override
	public Message<Map<String, Double>> receive() {

		List<Map<String, Double>> collect = this.cloudFoundryClient
				.getApplicationStats(this.applicationName)
				.getRecords()
				.stream()
				.map(this::instanceStatsMapFrom)
				.collect(Collectors.toList());

		Map<String, Double> avgs = new HashMap<>();
		avg(collect, avgs, UsageHeaders.CPU);
		avg(collect, avgs, UsageHeaders.DISK);
		avg(collect, avgs, UsageHeaders.MEM);
		return MessageBuilder.withPayload(avgs).build();
	}

	protected Map<String, Double> instanceStatsMapFrom(InstanceStats i) {
		Map<String, Double> m = new HashMap<>();
		InstanceStats.Usage usage = i.getUsage();
		m.put(UsageHeaders.CPU.toString(), usage.getCpu());
		m.put(UsageHeaders.DISK.toString(), Number.class.cast(usage.getDisk()).doubleValue());
		m.put(UsageHeaders.MEM.toString(), Number.class.cast(usage.getMem()).doubleValue());
		return m;
	}

	protected void avg(List<Map<String, Double>> collection, Map<String, Double> avgs, UsageHeaders h) {
		String key = h.toString();
		Double avgDouble = collection
				.stream()
				.map(m -> m.get(key))
				.collect(Collectors.averagingDouble(a -> a));
		avgs.put(key, avgDouble);
	}

	enum UsageHeaders {
		CPU, DISK, MEM
	}
}

