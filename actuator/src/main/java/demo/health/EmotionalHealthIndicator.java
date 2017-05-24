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
  this.event = event;
  this.when = new Date();
 }

 // <2>
 @Override
 protected void doHealthCheck(Health.Builder builder) throws Exception {
//@formatter:off
  Optional
   .ofNullable(this.event)
   .ifPresent(
    evt -> {
     Class<? extends EmotionalEvent> eventClass = this.event.getClass();
     Health.Builder healthBuilder = eventClass
             .isAssignableFrom(SadEvent.class) ? builder
      .down() : builder.up();
     String eventTimeAsString = this.when.toInstant().toString();
     healthBuilder.withDetail("class", eventClass).withDetail("when",
      eventTimeAsString);
    });
//@formatter:off
 }

}
