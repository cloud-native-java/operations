package cloudfoundry.metrics.source;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CloudFoundryUsageMetricsSourceApplication {

	public static void main(String[] args) {
		SpringApplication.run(CloudFoundryUsageMetricsSourceApplication.class, args);
	}
}
