package com.example;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.boot.actuate.audit.listener.AbstractAuditListener;
import org.springframework.boot.actuate.audit.listener.AuditApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
class SimpleAuditEventListener {

 private Log log = LogFactory.getLog(getClass());

 @EventListener(AuditApplicationEvent.class)
 public void onAuditEvent(AuditApplicationEvent event) {
  this.log.info("audit-event: " + event.toString());
 }
}
