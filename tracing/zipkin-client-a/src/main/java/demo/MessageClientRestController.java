package demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
class MessageClientRestController {

 @Autowired
 private RestTemplate restTemplate;

 @Value("${message-service}")
 private String host;

 @RequestMapping("/")
 Map<String, String> message() {

  //@formatter:off
  ParameterizedTypeReference<Map<String, String>> ptr =
          new ParameterizedTypeReference<Map<String, String>>() { };
  //@formatter:on

  return this.restTemplate.exchange(this.host, HttpMethod.GET, null, ptr)
   .getBody();
 }
}
