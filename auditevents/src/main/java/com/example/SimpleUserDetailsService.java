package com.example;

import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

@Service
class SimpleUserDetailsService implements UserDetailsService {

 private final Set<String> users = new ConcurrentSkipListSet<>();

 SimpleUserDetailsService() {
  // <1>
  this.users.addAll(Arrays.asList("pwebb", "dsyer", "mbhave", "snicoll",
   "awilkinson"));
 }

 @Override
 public UserDetails loadUserByUsername(String s)
  throws UsernameNotFoundException {
  // <2>
  return Optional
   .ofNullable(this.users.contains(s) ? s : null)
   .map(x -> new User(x, "pw", AuthorityUtils.createAuthorityList("ROLE_USER")))
   .orElseThrow(() -> new UsernameNotFoundException("couldn't find " + s + "!"));
 }
}
