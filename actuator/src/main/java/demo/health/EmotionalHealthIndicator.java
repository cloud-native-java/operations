package demo.health;

import demo.AbstractEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.metrics.CounterService;
import org.springframework.boot.actuate.metrics.GaugeService;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.Date;

@Component
class EmotionalHealthIndicator extends AbstractHealthIndicator {

    private AbstractEvent event;
    private CounterService counterService;
    private GaugeService gaugeService;
    private Date when;

    @Autowired
    public EmotionalHealthIndicator(GaugeService gs, CounterService cs) {
        this.counterService = cs;
        this.gaugeService = gs;
    }

    @Override
    protected void doHealthCheck(Health.Builder builder) throws Exception {
        builder.up()
                .withDetail("class", this.event.getClass())
                .withDetail("when", this.when.toInstant().toString());
        Assert.isTrue(this.event.getClass().isAssignableFrom(HappyEvent.class));
    }

    @EventListener
    public void onHealthEvent(AbstractEvent event) {
        this.emote(event);
    }

    protected void emote(AbstractEvent e) {
        this.event = e;
        this.gaugeService.submit("gauge." + e.getClass().getName(), Runtime.getRuntime().freeMemory());
        this.counterService.increment("meter." + e.getClass().getName());
        this.when = new Date();
    }
}
