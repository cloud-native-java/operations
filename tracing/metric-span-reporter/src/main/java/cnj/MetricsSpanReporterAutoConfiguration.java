package cnj;

import com.codahale.metrics.Meter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.actuate.metrics.CounterService;
import org.springframework.boot.actuate.metrics.GaugeService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.SpanReporter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass({ Meter.class, SpanReporter.class })
public class MetricsSpanReporterAutoConfiguration {

 @Bean
 public BeanPostProcessor metricSpanReportBeanPostProcessor(
  GaugeService gaugeService, CounterService counterService) {

  return new BeanPostProcessor() {

   @Override
   public Object postProcessBeforeInitialization(Object o, String s)
    throws BeansException {
    return o;
   }

   @Override
   public Object postProcessAfterInitialization(Object o, String s)
    throws BeansException {
    if (SpanReporter.class.isAssignableFrom(o.getClass())) {
     return new MetricSpanReporter(SpanReporter.class.cast(o));
    }
    return o;
   }

   class MetricSpanReporter implements SpanReporter {

    private final SpanReporter target;

    private Log log = LogFactory.getLog(getClass());

    MetricSpanReporter(SpanReporter target) {
     this.target = target;
    }

    @Override
    public void report(Span span) {
     log.info("received: " + span.toString());
     target.report(span);
     counterService.increment("meter.spans." + span.getName());
     gaugeService.submit("timer.spans." + span.getName(),
      span.getAccumulatedMicros());
    }
   }
  };
 }

}
