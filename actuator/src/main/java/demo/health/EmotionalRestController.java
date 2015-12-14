package demo.health;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class EmotionalRestController {

    private final ApplicationEventPublisher publisher;

    private final EmotionalHealthIndicator indicator;

    @Autowired
    public EmotionalRestController(ApplicationEventPublisher publisher,
                                   EmotionalHealthIndicator indicator) {
        this.indicator = indicator;
        this.publisher = publisher;
    }

    @RequestMapping("/event/happy")
    void eventHappy() {
        this.publisher.publishEvent(new HappyEvent());
    }

    @RequestMapping("/event/sad")
    void eventSad() {
        this.publisher.publishEvent(new SadEvent());
    }

    @RequestMapping("/indicator/happy")
    void indicatorHappy() {
        this.indicator.happy(new HappyEvent());
    }

    @RequestMapping("/indicator/sad")
    void indicatorSad() {
        this.indicator.sad(new SadEvent());
    }

}
