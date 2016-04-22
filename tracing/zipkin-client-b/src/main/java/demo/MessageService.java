package demo;

import com.codahale.metrics.Meter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.metrics.CounterService;
import org.springframework.boot.actuate.metrics.GaugeService;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cloud.sleuth.Sampler;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.SpanReporter;
import org.springframework.cloud.sleuth.sampler.AlwaysSampler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SpringBootApplication
public class MessageService {

	@Bean
	Sampler sampler() {
		return new AlwaysSampler();
	}

	public static void main(String[] args) {
		SpringApplication.run(MessageService.class, args);
	}
}


@RestController
class MessageServiceRestController {

	@RequestMapping("/")
	Map<String, String> message(HttpServletRequest httpRequest) {

		List<String> traceHeaders = Collections
				.list(httpRequest.getHeaderNames())
				.stream()
				.filter(h -> h.toLowerCase().startsWith("x-"))
				.collect(Collectors.toList());

		Map<String, String> response = new HashMap<>();
		response.put("message", "Hi, " + System.currentTimeMillis());
		traceHeaders.forEach(h -> response.put(h, httpRequest.getHeader(h)));
		return response;
	}

}
