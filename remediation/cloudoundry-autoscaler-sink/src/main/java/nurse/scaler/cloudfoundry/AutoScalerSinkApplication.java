package nurse.scaler.cloudfoundry;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.ExhaustedRetryException;

@SpringBootApplication
public class AutoScalerSinkApplication {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(AutoScalerSinkApplication.class, args);
    }
}
