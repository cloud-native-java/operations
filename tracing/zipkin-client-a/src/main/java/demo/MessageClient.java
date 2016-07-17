package demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.sleuth.Sampler;
import org.springframework.cloud.sleuth.sampler.AlwaysSampler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@EnableAspectJAutoProxy(proxyTargetClass = true)
@SpringBootApplication
public class MessageClient {

	@Bean
	RestTemplate restTemplate() {
		return new RestTemplate();
	}

	@Bean
	Sampler sampler() {
		return new AlwaysSampler();
	}

	public static void main(String[] args) {
		SpringApplication.run(MessageClient.class, args);
	}
}

@RestController
class MessageClientRestController {

	@Autowired
	private RestTemplate restTemplate;

	@Value("${message-service}")
	private String host;

	@RequestMapping("/")
	Map<String, String> message() {

		ParameterizedTypeReference<Map<String, String>> ptr = new ParameterizedTypeReference<Map<String, String>>() {
		};

		return this.restTemplate.exchange(this.host, HttpMethod.GET, null, ptr)
				.getBody();
	}
}
