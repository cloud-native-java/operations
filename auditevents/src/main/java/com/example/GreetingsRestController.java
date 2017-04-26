package com.example;

import org.springframework.beans.factory.annotation.Autowired;
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

 @Autowired
 GreetingsRestController(ApplicationEventPublisher appEventPublisher) {
  this.appEventPublisher = appEventPublisher;
 }

 @GetMapping("/hi")
 String greet(Principal p) { // <1>
  String msg = "hello, " + p.getName() + "!";

  AuditEvent auditEvent = new AuditEvent(p.getName(), // <2>
   GREETING_EVENT, // <3>
   Collections.singletonMap("greeting", msg)); // <4>

  this.appEventPublisher.publishEvent( // <5>
   new AuditApplicationEvent(auditEvent));

  return msg;
 }

}
