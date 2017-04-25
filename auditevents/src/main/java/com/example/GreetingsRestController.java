package com.example;

import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.boot.actuate.audit.AuditEventRepository;
import org.springframework.boot.actuate.audit.listener.AuditApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Collections;

@RestController
class GreetingsRestController {

 public static final String GREETING_EVENT = "greeting_event".toUpperCase();

 private final ApplicationEventPublisher appEventPublisher;

 public GreetingsRestController(ApplicationEventPublisher appEventPublisher) {
  this.appEventPublisher = appEventPublisher;
 }

 @GetMapping("/hi")
 String greet(Principal p) {
  String msg = "hello, " + p.getName() + "!";

  AuditEvent auditEvent = new AuditEvent(p.getName(), GREETING_EVENT,
   Collections.singletonMap("greeting", msg));

  this.appEventPublisher.publishEvent(new AuditApplicationEvent(auditEvent));

  return msg;
 }

}
