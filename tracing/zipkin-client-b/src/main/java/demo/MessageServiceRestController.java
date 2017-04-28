package demo;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
class MessageServiceRestController {

 @RequestMapping("/")
 Map<String, String> message(HttpServletRequest httpRequest) {

  List<String> traceHeaders = Collections.list(httpRequest.getHeaderNames())
   .stream().filter(h -> h.toLowerCase().startsWith("x-"))
   .collect(Collectors.toList()); // <1>

  Map<String, String> response = new HashMap<>();
  response.put("message", "Hi, @ " + System.currentTimeMillis());
  traceHeaders.forEach(h -> response.put(h, httpRequest.getHeader(h)));
  return response;
 }
}
