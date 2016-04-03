package demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.sleuth.Sampler;
import org.springframework.cloud.sleuth.sampler.AlwaysSampler;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
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
