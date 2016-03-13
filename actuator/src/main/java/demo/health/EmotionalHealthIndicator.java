package demo.health;

import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Optional;

@Component
class EmotionalHealthIndicator extends AbstractHealthIndicator {

	private EmotionalEvent event;
	private Date when;

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
		this.when = new Date();
	}
}
