package demo.health;

import org.springframework.context.ApplicationEvent;

public abstract class EmotionalEvent extends ApplicationEvent {

 public EmotionalEvent(String source) {
  super(source);
 }

 public EmotionalEvent() {
  this("");
 }
}
