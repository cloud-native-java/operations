package demo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@SpringBootApplication
@RestController
public class LoggingApplication {

 private Log log = LogFactory.getLog(getClass());

 public static void main(String args[]) {
  SpringApplication.run(LoggingApplication.class, args);
 }

 @PostMapping("/log")
 public void triggerLog(@RequestParam Optional<String> name) {
  String greeting = "Hello, world!";
  this.log.info("INFO: " + greeting);
  this.log.warn("WARN: " + greeting); // <1>
  this.log.debug("DEBUG: " + greeting);
 }
}
