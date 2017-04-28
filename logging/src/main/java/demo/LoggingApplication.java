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

import javax.annotation.PostConstruct;
import java.util.Optional;

@SpringBootApplication
@RestController
public class LoggingApplication {

 private Log log = LogFactory.getLog(getClass());

 public static void main(String args[]) {
  SpringApplication.run(LoggingApplication.class, args);
 }

 LoggingApplication() {
  triggerLog(Optional.empty());
 }

 @GetMapping("/log")
 public void triggerLog(@RequestParam Optional<String> name) {
  String greeting = "Hello, " + name.orElse("World") + "!";
  this.log.warn("WARN: " + greeting); // <1>
  this.log.info("INFO: " + greeting);
  this.log.debug("DEBUG: " + greeting);
  this.log.error("ERROR: " + greeting);
 }
}
