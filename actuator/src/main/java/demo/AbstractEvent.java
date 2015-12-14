package demo;

import org.springframework.context.ApplicationEvent;


public abstract class AbstractEvent extends ApplicationEvent {

    public AbstractEvent(String source) {
        super(source);
    }

    public AbstractEvent() {
        this("");
    }
}
