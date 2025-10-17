package com.tariff.backend.service;

import com.tariff.backend.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

  private UserRepository userRepository;

  public UserDetailsServiceImpl(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  // Retrieve user using UserRepository for Spring Security
  @Override
  public UserDetails loadUserByUsername(String username) {
    return userRepository
      .findByEmail(username)
      .orElseThrow(() ->
        new UsernameNotFoundException(
          "We couldn't find an account with that email. Please check your details."
        )
      );
  }
}
