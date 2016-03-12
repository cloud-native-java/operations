package demo.metrics;

import demo.Customer;
import demo.metrics.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.PublicMetrics;
import org.springframework.boot.actuate.metrics.CounterService;
import org.springframework.boot.actuate.metrics.Metric;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@RestController
@RequestMapping("/customers")
public class CustomerRestController {

	private final CounterService counterService;
	private final CustomerRepository customerRepository;

	@Autowired
	CustomerRestController(CustomerRepository repository,
	                       CounterService counterService) {
		this.customerRepository = repository;
		this.counterService = counterService;
	}

	@RequestMapping(method = RequestMethod.GET, value = "/{id}")
	Customer get(@PathVariable Long id) {
		return this.customerRepository.findById(id)
			.map(customer -> {
				this.counterService.increment("customers.read.found");
				return customer;
			})
			.orElseGet(() -> {
				this.counterService.increment("customers.read.not-found");
				return null;
			});
	}

	@RequestMapping(method = RequestMethod.POST)
	void add(@RequestBody Customer newCustomer) {
		this.customerRepository.save(newCustomer);
		this.counterService.increment("customers.create");
	}

	@RequestMapping(method = RequestMethod.DELETE)
	void delete(@PathVariable Long id) {
		this.customerRepository.delete(id);
		this.counterService.increment("customers.delete");
	}

}
