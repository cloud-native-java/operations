package demo.health;

import demo.AbstractEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.metrics.CounterService;
import org.springframework.boot.actuate.metrics.GaugeService;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
class EmotionalHealthIndicator
        implements HealthIndicator {

    private CounterService counterService;
    private GaugeService gaugeService;
    private volatile Health lastKnownHealth;
    private volatile Date when;

    @Autowired
    public EmotionalHealthIndicator(GaugeService gs,
                                    CounterService cs) {
        this.counterService = cs;
        this.gaugeService = gs;
        this.happy(new HappyEvent());
    }

    @Override
    public synchronized Health health() {
        return lastKnownHealth;
    }

    @EventListener
    public void onHealthEvent(AbstractEvent event) {
        if (event.getClass().isAssignableFrom(SadEvent.class)) {
            sad(SadEvent.class.cast(event));
        } else if (event.getClass().isAssignableFrom(HappyEvent.class)) {
            happy(HappyEvent.class.cast(event));
        }
    }

    protected void emote(Health.Builder builder, AbstractEvent e) {
        this.gaugeService.submit("gauge." + e.getClass().getName(), Runtime.getRuntime().freeMemory());
        this.counterService.increment("meter." + e.getClass().getName());
        this.when = new Date();
        this.lastKnownHealth = builder
                .withDetail("class", e.getClass())
                .withDetail("when", this.when.toInstant().toString())
                .build();
    }

    public void happy(HappyEvent e) {
        this.emote(Health.up(), e);
    }

    public void sad(SadEvent e) {
        this.emote(Health.down(), e);
    }
}
