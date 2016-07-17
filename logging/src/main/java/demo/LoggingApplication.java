package demo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LoggingApplication implements CommandLineRunner {

	public static void main(String args[]) {
		SpringApplication.run(LoggingApplication.class, args);
	}

	private Log log = LogFactory.getLog(getClass());

	@Override
	public void run(String... args) throws Exception {

		String greeting = "Hello, world!";

		this.log.info("INFO: " + greeting);
		this.log.warn("WARN: " + greeting); // <1>
		this.log.debug("DEBUG: " + greeting);
	}
}
