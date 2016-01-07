package nurse.logging.cloudfoundry;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LoggregatorSourceApplication {

    public static void main(String[] args) {
        SpringApplication.run(LoggregatorSourceApplication.class, args);
    }
}
