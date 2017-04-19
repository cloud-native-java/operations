package demo.metrics;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.metrics.CounterService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/metered/customers")
public class MeterCustomerRestController extends CustomerRestController {

 @Autowired
 MeterCustomerRestController(CustomerRepository repository,
  CounterService counterService) {
  super(repository, counterService);
 }

 @Override
 protected String metricPrefix(String k) {
  return "meter." + k; // <1>
 }
}
