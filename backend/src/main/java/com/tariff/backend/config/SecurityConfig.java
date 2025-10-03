package com.tariff.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.util.StringUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {
  @Value("${FRONTEND_EC2_HOST:localhost}")
  private String frontendHost;

  @Bean
  SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
      .cors(cors -> cors.configurationSource(corsConfigurationSource())) // Enable CORS with custom config
      .csrf(csrf -> csrf.disable()) // Disable CSRF for APIs
      .authorizeHttpRequests(auth -> auth
        .requestMatchers("/api/users/login", "/api/users/register").permitAll()
        .anyRequest().permitAll()
      )
      .httpBasic(Customizer.withDefaults()); // Optional: Basic auth for other endpoints

    return http.build();
  }

  @Bean
  public BCryptPasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(List.of(resolveFrontendOrigin()));
    configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
    configuration.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }

  private String resolveFrontendOrigin() {
    if (!StringUtils.hasText(frontendHost)) {
      return "http://localhost:3000";
    }

    String origin = frontendHost.trim();
    if (origin.contains("://")) {
      return origin;
    }

    boolean hasPort = origin.contains(":");
    return "http://" + origin + (hasPort ? "" : ":3000");
  }
}
