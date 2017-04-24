package com.example;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

@Service
class SimpleUserDetailsService implements UserDetailsService {

 private final Map<String, UserDetails> details = new ConcurrentHashMap<>();

 private final Log log = LogFactory.getLog(getClass());

 SimpleUserDetailsService() {
  Stream
   .of("pwebb", "dsyer", "mbhave", "snicoll", "awilkinson")
   .map(n -> new User(n, "pw", AuthorityUtils.createAuthorityList("ROLE_USER")))
   .forEach(u -> this.details.put(u.getUsername(), u));

  this.details.forEach((k, v) -> log.info(k + '=' + v));
 }

 @Override
 public UserDetails loadUserByUsername(String s)
  throws UsernameNotFoundException {
  return Optional.ofNullable(this.details.get(s)).orElseThrow(
   () -> new UsernameNotFoundException("couldn't find " + s + "!"));
 }
}
