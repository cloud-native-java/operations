package demo.metrics;

import demo.Customer;
import demo.metrics.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.PublicMetrics;
import org.springframework.boot.actuate.metrics.CounterService;
import org.springframework.boot.actuate.metrics.Metric;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.xml.crypto.URIReference;
import java.net.URI;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

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
		return this.customerRepository.findById(id)
				.map(customer -> {
					this.counterService.increment(
							this.metricPrefix("customers.read.found")); // <2>
					return ResponseEntity.ok(customer);
				})
				.orElseGet(() -> {
					this.counterService.increment(
							this.metricPrefix("customers.read.not-found")); // <3>
					return ResponseEntity.class.cast(
							ResponseEntity.notFound().build());
				});
	}

	@RequestMapping(method = RequestMethod.POST)
	ResponseEntity<?> add(@RequestBody Customer newCustomer) {
		this.customerRepository.save(newCustomer);
		ServletUriComponentsBuilder url = ServletUriComponentsBuilder.fromCurrentRequest();
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
