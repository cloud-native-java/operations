package cloudfoundry.autoscaler.sink;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AutoScalerSinkApplication {

	public static void main(String[] args) throws Exception {
		SpringApplication.run(AutoScalerSinkApplication.class, args);
	}
}
