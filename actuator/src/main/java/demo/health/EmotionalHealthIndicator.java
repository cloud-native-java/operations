package demo.health;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.metrics.CounterService;
import org.springframework.boot.actuate.metrics.GaugeService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Optional;

@Component
class EmotionalHealthIndicator extends AbstractHealthIndicator {

	private EmotionalEvent event;
	private CounterService counterService;
	private GaugeService gaugeService;
	private Date when;

	@Autowired
	public EmotionalHealthIndicator(GaugeService gs, CounterService cs) {
		this.counterService = cs;
		this.gaugeService = gs;
	}

	// <1>
	@EventListener
	public void onHealthEvent(EmotionalEvent event) {
		this.emote(event);
	}

	// <2>
	@Override
	protected void doHealthCheck(Health.Builder builder) throws Exception {

		Optional.ofNullable(this.event).ifPresent(evt -> {
			Class<? extends EmotionalEvent> eventClass = this.event.getClass();
			Health.Builder healthBuilder = eventClass.isAssignableFrom(SadEvent.class) ?
					builder.down() : builder.up();
			healthBuilder
					.withDetail("class", eventClass)
					.withDetail("when", this.when.toInstant().toString());
		});
	}

	protected void emote(EmotionalEvent e) {
		this.event = e;
		this.gaugeService.submit("gauge." + e.getClass().getName(), Runtime.getRuntime().freeMemory());
		this.counterService.increment("meter." + e.getClass().getName());
		this.when = new Date();
	}
}
