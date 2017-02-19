package demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.dataflow.server.EnableDataFlowServer;

@SpringBootApplication
@EnableDataFlowServer
public class DataflowServerApplication {

 public static void main(String[] args) {
  SpringApplication.run(DataflowServerApplication.class, args);
 }
}
