package demo.metrics;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.metrics.CounterService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/customers")
public class CustomerRestController {

 private final CounterService counterService; // <1>

 private final CustomerRepository customerRepository;

 @Autowired
 CustomerRestController(CustomerRepository repository,
  CounterService counterService) {
  this.customerRepository = repository;
  this.counterService = counterService;
 }

 @RequestMapping(method = RequestMethod.GET, value = "/{id}")
 ResponseEntity<?> get(@PathVariable Long id) {
  return this.customerRepository.findById(id).map(customer -> {
   String metricName = metricPrefix("customers.read.found");
   this.counterService.increment(metricName); // <2>
   return ResponseEntity.ok(customer);
  }).orElseGet(() -> {
   String metricName = metricPrefix("customers.read.not-found");
   this.counterService.increment(metricName); // <3>
   return ResponseEntity.class.cast(ResponseEntity.notFound().build());
  });
 }

 @RequestMapping(method = RequestMethod.POST)
 ResponseEntity<?> add(@RequestBody Customer newCustomer) {
  this.customerRepository.save(newCustomer);
  ServletUriComponentsBuilder url = ServletUriComponentsBuilder
   .fromCurrentRequest();
  URI location = url.path("/{id}").buildAndExpand(newCustomer.getId()).toUri();
  return ResponseEntity.created(location).build();
 }

 @RequestMapping(method = RequestMethod.DELETE)
 ResponseEntity<?> delete(@PathVariable Long id) {
  this.customerRepository.delete(id);
  return ResponseEntity.notFound().build();
 }

 @RequestMapping(method = RequestMethod.GET)
 ResponseEntity<?> get() {
  return ResponseEntity.ok(this.customerRepository.findAll());
 }

 // <4>
 protected String metricPrefix(String k) {
  return k;
 }

}
