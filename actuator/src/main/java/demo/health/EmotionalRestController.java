package demo.health;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class EmotionalRestController {

 private final ApplicationEventPublisher publisher; // <1>

 @Autowired
 EmotionalRestController(ApplicationEventPublisher publisher) {
  this.publisher = publisher;
 }

 @RequestMapping("/event/happy")
 void eventHappy() {
  this.publisher.publishEvent(new HappyEvent()); // <2>
 }

 @RequestMapping("/event/sad")
 void eventSad() {
  this.publisher.publishEvent(new SadEvent());
 }
}
