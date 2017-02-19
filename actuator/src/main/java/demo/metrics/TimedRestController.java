package demo.metrics;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.metrics.GaugeService;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TimedRestController {

 private final GaugeService gaugeService;

 @Autowired
 public TimedRestController(GaugeService gaugeService) {
  this.gaugeService = gaugeService;
 }

 @RequestMapping(method = RequestMethod.GET, value = "/timer/hello")
 ResponseEntity<?> hello() throws Exception {
  StopWatch sw = new StopWatch(); // <1>
  sw.start();
  try {
   Thread.sleep((long) (Math.random() * 60) * 1000);
   return ResponseEntity.ok("Hi, " + System.currentTimeMillis());
  }
  finally {
   sw.stop();
   this.gaugeService.submit("timer.hello", sw.getLastTaskTimeMillis());
  }
 }

}
