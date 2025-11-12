package com.tariff.backend.component;

import com.tariff.backend.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

// auth middleware component
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

  private final HandlerExceptionResolver handlerExceptionResolver;

  private final JwtService jwtService;

  private final UserDetailsService userDetailsService;

  public JwtAuthFilter(
    JwtService jwtService,
    UserDetailsService userDetailsService,
    HandlerExceptionResolver handlerExceptionResolver
  ) {
    this.jwtService = jwtService;
    this.userDetailsService = userDetailsService;
    this.handlerExceptionResolver = handlerExceptionResolver;
  }

  @Override
  protected void doFilterInternal(
    // idk, if this code got problem check if this the isssue
    HttpServletRequest request,
    HttpServletResponse response,
    FilterChain filterChain
  ) throws ServletException, IOException {
    final String authHeader = request.getHeader("Authorization");

    // retrieve JWT token in "authorization" header
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      filterChain.doFilter(request, response);
      return;
    }

    try {
      // extract header, email
      final String jwt = authHeader.substring(7);
      final String userEmail = jwtService.extractUsername(jwt);

      // check is user have been authenticated before
      Authentication authentication = SecurityContextHolder.getContext()
        .getAuthentication();

      // check if email is not null and whether user been autheticated before,
      // uses && to short-circuit
      if (userEmail != null && authentication == null) {
        //  if pass condition abv, get email from db
        // (?) to-do: implementng caching to find user by email to improve performance?
        UserDetails userDetails =
          this.userDetailsService.loadUserByUsername(userEmail);

        // check if token valid
        if (jwtService.isTokenValid(jwt, userDetails)) {
          // set auth context using email from db
          UsernamePasswordAuthenticationToken authToken =
            new UsernamePasswordAuthenticationToken(
              userDetails,
              null,
              userDetails.getAuthorities()
            );

          authToken.setDetails(
            new WebAuthenticationDetailsSource().buildDetails(request)
          );

          // muhaha now i can access in any application layer
          SecurityContextHolder.getContext().setAuthentication(authToken);
        }
      }

      // pass the request and response to next filer
      filterChain.doFilter(request, response);
    } catch (Exception exception) {
      // forward error to global exception handler
      handlerExceptionResolver.resolveException(
        request,
        response,
        null,
        exception
      );
    }
  }
}
