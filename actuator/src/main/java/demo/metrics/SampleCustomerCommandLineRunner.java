package demo.metrics;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.stream.Stream;

@Component
public class SampleCustomerCommandLineRunner implements CommandLineRunner {

 private final CustomerRepository customerRepository;

 @Autowired
 public SampleCustomerCommandLineRunner(CustomerRepository customerRepository) {
  this.customerRepository = customerRepository;
 }

 @Override
 public void run(String... args) throws Exception {
  Stream.of("Kenny", "Josh", "Phil", "Dave", "Spencer", "Andrew").forEach(
   x -> this.customerRepository.save(new Customer(x.toLowerCase()
    + "@email.com", x)));
 }
}