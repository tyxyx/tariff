package com.tariff.backend.config;

import com.tariff.backend.component.JwtAuthFilter;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  @Value("${FRONTEND_EC2_HOST}")
  private String frontendHost;

  private final AuthenticationProvider authenticationProvider;
  private final JwtAuthFilter jwtAuthenticationFilter;

  public SecurityConfig(
    JwtAuthFilter jwtAuthenticationFilter,
    AuthenticationProvider authenticationProvider
  ) {
    this.authenticationProvider = authenticationProvider;
    this.jwtAuthenticationFilter = jwtAuthenticationFilter;
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
      // CORS configuration
      .cors(Customizer.withDefaults())
      //   disable csrf for token auth
      .csrf(AbstractHttpConfigurer::disable)
      //   ensure request is stateless
      .sessionManagement(c ->
        c.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
      )
      //   use custom auth provider
      .authenticationProvider(authenticationProvider)
      //   use auth middleware component
      .addFilterBefore(
        jwtAuthenticationFilter,
        UsernamePasswordAuthenticationFilter.class
      )
      // identify path to auth
  .authorizeHttpRequests(request ->
      request
          // 1. Public Endpoints (Be specific!)
          .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
          .requestMatchers(HttpMethod.POST, "/api/users/register").permitAll() // Assuming this is for registration
          .requestMatchers(HttpMethod.POST, "/api/users/login").permitAll() // Assuming you have a login endpoint
          .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()

          // 2. User "Self-Service" Rules (Authenticated)
          // Placed *before* Admin rules to be matched first
          .requestMatchers(HttpMethod.PUT, "/api/users/me/password").authenticated() // For changing *own* password
          .requestMatchers(HttpMethod.GET, "/api/users/me").authenticated() // For getting *own* profile

          // 3. SUPER_ADMIN Rules (Most specific roles first)
          .requestMatchers(HttpMethod.PUT, "/api/users/downgrade-role").hasRole("SUPER_ADMIN")

          // 4. ADMIN and SUPER_ADMIN Rules
          .requestMatchers(HttpMethod.GET, "/api/users").hasAnyRole("ADMIN", "SUPER_ADMIN") // Plan: "admin can view all"
          .requestMatchers(HttpMethod.PUT, "/api/users/upgrade-role").hasAnyRole("ADMIN", "SUPER_ADMIN") // Plan: "admin can upgrade user"

          // No specific delete for super-admin; it's covered by the ADMIN/SUPER_ADMIN rule below.
          // The service layer will check if they are deleting an admin.
          .requestMatchers(HttpMethod.DELETE, "/api/users/*").hasAnyRole("ADMIN", "SUPER_ADMIN")

          // 5. Deny all other requests by default (unless authenticated)
          .anyRequest().authenticated()
  );

    return http.build();
  }

  @Bean
  CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(List.of("http://localhost:3000")); // Replace with your frontend URL
    configuration.setAllowedMethods(
      // List.of("GET", "POST", "PUT", "DELETE", "OPTIONS")
      List.of("GET", "POST", "PUT","DELETE")
    );
    configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
    configuration.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source =
      new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }
}
