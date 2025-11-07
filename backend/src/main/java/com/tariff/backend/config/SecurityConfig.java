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
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.SimpleAuthorityMapper;
import org.springframework.security.config.core.GrantedAuthorityDefaults;

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
          .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
          // Register Users
          .requestMatchers(
            HttpMethod.POST,
            "/api/users/login",
            "/api/users/register"
          ).permitAll()

          // Only admin can get all user details
          .requestMatchers(HttpMethod.GET, "/api/users/").hasRole("ADMIN")

          // Only admin can edit user password
          .requestMatchers(HttpMethod.PUT, "/api/users/*").hasRole("ADMIN")
          .requestMatchers(HttpMethod.DELETE, "/api/users/*").hasRole("ADMIN")
          
          // Swagger UI
          .requestMatchers(
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/swagger-ui.html"
          ).permitAll()

          // Forbid all other requests by default
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
