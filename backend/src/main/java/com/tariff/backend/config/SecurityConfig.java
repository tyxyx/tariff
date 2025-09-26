package com.tariff.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

  @Bean
  SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
      .cors(cors -> {})  // Enable CORS
      .csrf(csrf -> csrf.disable()) // not needed for simple GETs/APIs
      .authorizeHttpRequests(auth -> auth
        .requestMatchers("/api/users/login", "/api/users/register").permitAll()
        // .anyRequest().authenticated()
        .anyRequest().permitAll()
      )
      .httpBasic(Customizer.withDefaults()); // keep basic auth for other endpoints (optional)

    return http.build();
  }

  @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
