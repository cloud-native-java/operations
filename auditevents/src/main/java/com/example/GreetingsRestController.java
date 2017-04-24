package com.example;

import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.boot.actuate.audit.AuditEventRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Collections;

@RestController
class GreetingsRestController {

 public static final String GREETING_EVENT = "greeting_event".toUpperCase();

 private final AuditEventRepository auditEventRepository;

 public GreetingsRestController(AuditEventRepository auditEventRepository) {
  this.auditEventRepository = auditEventRepository;
 }

 @GetMapping("/hi")
 String greet(Principal p) {
  String msg = "hello, " + p.getName() + "!";
  this.auditEventRepository.add(new AuditEvent(p.getName(), GREETING_EVENT,
   Collections.singletonMap("greeting", msg)));
  return msg;
 }

}
