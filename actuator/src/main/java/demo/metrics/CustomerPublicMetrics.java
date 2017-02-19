package demo.metrics;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.PublicMetrics;
import org.springframework.boot.actuate.metrics.Metric;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Component
class CustomerPublicMetrics implements PublicMetrics {

 private final CustomerRepository customerRepository;

 @Autowired
 public CustomerPublicMetrics(CustomerRepository customerRepository) {
  this.customerRepository = customerRepository;
 }

 @Override
 public Collection<Metric<?>> metrics() {

  Set<Metric<?>> metrics = new HashSet<>();

  long count = this.customerRepository.count();

  // <1>
  Metric<Number> customersCountMetric = new Metric<>("customers.count", count);
  metrics.add(customersCountMetric);
  return metrics;
 }
}