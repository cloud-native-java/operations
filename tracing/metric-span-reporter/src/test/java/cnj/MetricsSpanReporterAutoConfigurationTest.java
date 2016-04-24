package cnj;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.embedded.EmbeddedServletContainerInitializedEvent;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

public class MetricsSpanReporterAutoConfigurationTest {

	private static CountDownLatch countDownLatch = new CountDownLatch(2);

	private ApplicationContext a, b;
	private static final Map<String, Integer> contexts = new ConcurrentHashMap<>();

	public abstract static class Base {


		protected abstract String getIdentifier();

		@RequestMapping("/hi")
		Map<String, String> hi() {
			return Collections.singletonMap("message", "Hi from " + getIdentifier());
		}

		@Component
		public static class CDLAL {

			@EventListener(ApplicationReadyEvent.class)
			public void applicationReady(ApplicationReadyEvent event) {
				countDownLatch.countDown();
			}
		}


		@Bean
		ESCAL escal() {
			return new ESCAL(getIdentifier());
		}

		public static class ESCAL {

			private final String identifier;

			public ESCAL(String x) {
				this.identifier = x;
			}

			@EventListener(EmbeddedServletContainerInitializedEvent.class)
			public void containerInitialized(EmbeddedServletContainerInitializedEvent e) {
				int port = e.getEmbeddedServletContainer().getPort();
				contexts.put(identifier, port);
			}
		}

	}

	private static final Log log = LogFactory.getLog(MetricsSpanReporterAutoConfigurationTest.class);

	@EnableAutoConfiguration
	@Configuration
	@RestController
	public static class A extends Base {
		@Override
		protected String getIdentifier() {
			return "a";
		}

		private RestTemplate restTemplate = new RestTemplate();

		@RequestMapping("/client")
		ResponseEntity<String> client(@RequestParam String service) {
			return ResponseEntity.ok(
					this.restTemplate.getForEntity(service, String.class).getBody());
		}
	}

	@EnableAutoConfiguration
	@Configuration
	@RestController
	public static class B extends Base {

		@RequestMapping("/service")
		public Map<String, String> hello() {
			return Collections.singletonMap("message", "Hello, client!");
		}

		@Override
		protected String getIdentifier() {
			return "b";
		}
	}

	private RestTemplate restTemplate = new RestTemplate();

	@Before
	public void before() throws Exception {

		this.a = SpringApplication.run(A.class);
		this.b = SpringApplication.run(B.class);

		countDownLatch.await();

		log.info("the applications are ready!");
		Assert.assertEquals(contexts.size(), 2);
		contexts.entrySet().forEach(e -> {

			ResponseEntity<Map<String, String>> responseEntity =
					this.restTemplate.exchange(
							"http://localhost:" + e.getValue() + "/hi",
							HttpMethod.GET,
							null,
							new ParameterizedTypeReference<Map<String, String>>() {
							});


			Map<String, String> body = responseEntity.getBody();
			log.info("result from calling '" + e.getKey() + "': " + body);
			String message = body.get("message");
			Assert.assertNotNull(message);
			Assert.assertTrue(message.contains("Hi from"));
		});


		int portForB = contexts.get("b"),
				portForA = contexts.get("a");

		String url = "http://localhost:" + portForB + "/service";
		Map<String, String> msg = this.restTemplate.exchange(url, HttpMethod.GET, null, new ParameterizedTypeReference<Map<String, String>>() {
		}).getBody();
		Assert.assertEquals(msg.get("message"), "Hello, client!");
		log.info(msg);

		Arrays.asList( portForA , portForB  ).forEach(port -> log.info(this.restTemplate.exchange("http://localhost:" + port + "/metrics", HttpMethod.GET, null, String.class)));

	}


	@Test
	public void testMetrics() throws Exception {

	}
}