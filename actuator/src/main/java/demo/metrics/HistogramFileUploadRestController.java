package demo.metrics;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.metrics.GaugeService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/histogram/uploads")
public class HistogramFileUploadRestController {

 private final GaugeService gaugeService;

 private Log log = LogFactory.getLog(getClass());

 @Autowired
 HistogramFileUploadRestController(GaugeService gaugeService) {
  this.gaugeService = gaugeService;
 }

 @RequestMapping(method = RequestMethod.POST)
 void upload(@RequestParam MultipartFile file) {
  long size = file.getSize();
  this.log.info(String.format("received %s with file size %s",
   file.getOriginalFilename(), size));
  this.gaugeService.submit("histogram.file-uploads.size", size); // <1>
 }
}
