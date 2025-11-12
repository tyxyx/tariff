package com.tariff.backend.component;

import com.tariff.backend.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JwtAuthFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private HandlerExceptionResolver handlerExceptionResolver;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private JwtAuthFilter jwtAuthFilter;

    @BeforeEach
    public void setUp() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void doFilterInternal_withValidToken_shouldSetAuthentication() throws ServletException, IOException {
        String token = "valid-jwt-token";
        String userEmail = "user@test.com";

        Cookie authCookie = new Cookie("auth_token", token);
        Cookie[] cookies = {authCookie};

        when(request.getCookies()).thenReturn(cookies);
        when(jwtService.extractUsername(token)).thenReturn(userEmail);
        when(userDetailsService.loadUserByUsername(userEmail)).thenReturn(userDetails);
        when(jwtService.isTokenValid(token, userDetails)).thenReturn(true);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
        verify(userDetailsService).loadUserByUsername(userEmail);
        verify(jwtService).isTokenValid(token, userDetails);
    }

    @Test
    public void doFilterInternal_withNoCookies_shouldContinueFilterChain() throws ServletException, IOException {
        when(request.getCookies()).thenReturn(null);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
        verify(jwtService, never()).extractUsername(any());
    }

    @Test
    public void doFilterInternal_withNoAuthToken_shouldContinueFilterChain() throws ServletException, IOException {
        Cookie otherCookie = new Cookie("other_cookie", "value");
        Cookie[] cookies = {otherCookie};

        when(request.getCookies()).thenReturn(cookies);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
        verify(jwtService, never()).extractUsername(any());
    }

    @Test
    public void doFilterInternal_withInvalidToken_shouldNotSetAuthentication() throws ServletException, IOException {
        String token = "invalid-jwt-token";
        String userEmail = "user@test.com";

        Cookie authCookie = new Cookie("auth_token", token);
        Cookie[] cookies = {authCookie};

        when(request.getCookies()).thenReturn(cookies);
        when(jwtService.extractUsername(token)).thenReturn(userEmail);
        when(userDetailsService.loadUserByUsername(userEmail)).thenReturn(userDetails);
        when(jwtService.isTokenValid(token, userDetails)).thenReturn(false);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
        verify(jwtService).isTokenValid(token, userDetails);
    }

    @Test
    public void doFilterInternal_withException_shouldCallExceptionResolver() throws ServletException, IOException {
        String token = "valid-jwt-token";
        Cookie authCookie = new Cookie("auth_token", token);
        Cookie[] cookies = {authCookie};

        when(request.getCookies()).thenReturn(cookies);
        when(jwtService.extractUsername(token)).thenThrow(new RuntimeException("Token parsing error"));

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(handlerExceptionResolver).resolveException(
                eq(request),
                eq(response),
                eq(null),
                any(RuntimeException.class)
        );
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    public void doFilterInternal_withAlreadyAuthenticatedUser_shouldNotReauthenticate() throws ServletException, IOException {
        String token = "valid-jwt-token";
        String userEmail = "user@test.com";

        Cookie authCookie = new Cookie("auth_token", token);
        Cookie[] cookies = {authCookie};

        // Set existing authentication
        org.springframework.security.core.Authentication existingAuth = mock(org.springframework.security.core.Authentication.class);
        SecurityContextHolder.getContext().setAuthentication(existingAuth);

        when(request.getCookies()).thenReturn(cookies);
        when(jwtService.extractUsername(token)).thenReturn(userEmail);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(userDetailsService, never()).loadUserByUsername(any());
        verify(jwtService, never()).isTokenValid(any(), any());
        verify(filterChain).doFilter(request, response);
    }
}
