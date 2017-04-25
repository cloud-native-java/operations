package com.example;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.boot.actuate.audit.listener.AbstractAuditListener;
import org.springframework.stereotype.Component;

@Component
class SimpleAuditEventListener extends AbstractAuditListener {

 private Log log = LogFactory.getLog(getClass());

 @Override
 protected void onAuditEvent(AuditEvent event) {
  this.log.info("audit-event: " + event.toString());
 }
}
