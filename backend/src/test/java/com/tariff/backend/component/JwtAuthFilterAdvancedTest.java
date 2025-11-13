package com.tariff.backend.component;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import com.tariff.backend.service.JwtService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@ExtendWith(MockitoExtension.class)
public class JwtAuthFilterAdvancedTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private UserDetails userDetails;

    @Mock
    private org.springframework.web.servlet.HandlerExceptionResolver handlerExceptionResolver;

    @InjectMocks
    private JwtAuthFilter jwtAuthFilter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldHandleMultipleCookies() throws ServletException, IOException {
        Cookie[] cookies = {
            new Cookie("session_id", "abc123"),
            new Cookie("auth_token", "valid.jwt.token"),
            new Cookie("preferences", "dark_mode")
        };
        when(request.getCookies()).thenReturn(cookies);
        when(jwtService.extractUsername("valid.jwt.token")).thenReturn("testuser");
        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(userDetails);
        when(jwtService.isTokenValid("valid.jwt.token", userDetails)).thenReturn(true);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void shouldHandleEmptyAuthToken() throws ServletException, IOException {
        Cookie[] cookies = {
            new Cookie("auth_token", "")
        };
        when(request.getCookies()).thenReturn(cookies);
        when(jwtService.extractUsername("")).thenReturn(null);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void shouldHandleWhitespaceAuthToken() throws ServletException, IOException {
        Cookie[] cookies = {
            new Cookie("auth_token", "   ")
        };
        when(request.getCookies()).thenReturn(cookies);
        when(jwtService.extractUsername("   ")).thenReturn(null);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void shouldHandleNullUsernameFromToken() throws ServletException, IOException {
        Cookie[] cookies = {
            new Cookie("auth_token", "invalid.jwt.token")
        };
        when(request.getCookies()).thenReturn(cookies);
        when(jwtService.extractUsername("invalid.jwt.token")).thenReturn(null);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(userDetailsService, never()).loadUserByUsername(anyString());
    }

    @Test
    void shouldHandleEmptyUsernameFromToken() throws ServletException, IOException {
        Cookie[] cookies = {
            new Cookie("auth_token", "invalid.jwt.token")
        };
        when(request.getCookies()).thenReturn(cookies);
        when(jwtService.extractUsername("invalid.jwt.token")).thenReturn("");
        // Empty string is not null, so loadUserByUsername will be called
        when(userDetailsService.loadUserByUsername("")).thenThrow(new RuntimeException("User not found"));

        // The filter catches exceptions, so it won't throw
        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        // Exception was handled, authentication should still be null
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void shouldHandleUserDetailsServiceException() throws ServletException, IOException {
        Cookie[] cookies = {
            new Cookie("auth_token", "valid.jwt.token")
        };
        when(request.getCookies()).thenReturn(cookies);
        when(jwtService.extractUsername("valid.jwt.token")).thenReturn("testuser");
        when(userDetailsService.loadUserByUsername("testuser"))
            .thenThrow(new RuntimeException("User not found"));

        // The filter catches exceptions and delegates to handlerExceptionResolver
        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        // Verify exception was handled
        verify(handlerExceptionResolver).resolveException(eq(request), eq(response), eq(null), any(RuntimeException.class));
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void shouldNotAuthenticateWhenTokenIsInvalid() throws ServletException, IOException {
        Cookie[] cookies = {
            new Cookie("auth_token", "expired.jwt.token")
        };
        when(request.getCookies()).thenReturn(cookies);
        when(jwtService.extractUsername("expired.jwt.token")).thenReturn("testuser");
        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(userDetails);
        when(jwtService.isTokenValid("expired.jwt.token", userDetails)).thenReturn(false);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void shouldHandleAlreadyAuthenticatedUser() throws ServletException, IOException {
        // Simulate existing authentication
        org.springframework.security.authentication.UsernamePasswordAuthenticationToken existingAuth =
            new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
            );
        SecurityContextHolder.getContext().setAuthentication(existingAuth);

        Cookie[] cookies = {
            new Cookie("auth_token", "valid.jwt.token")
        };
        when(request.getCookies()).thenReturn(cookies);
        when(jwtService.extractUsername("valid.jwt.token")).thenReturn("testuser");

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        // Should not load user details again
        verify(userDetailsService, never()).loadUserByUsername(anyString());
    }

    @Test
    void shouldHandleCookieWithSpecialCharacters() throws ServletException, IOException {
        Cookie[] cookies = {
            new Cookie("auth_token", "token.with.special!@#$%^&*()chars")
        };
        when(request.getCookies()).thenReturn(cookies);
        when(jwtService.extractUsername("token.with.special!@#$%^&*()chars")).thenReturn("testuser");
        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(userDetails);
        when(jwtService.isTokenValid("token.with.special!@#$%^&*()chars", userDetails)).thenReturn(true);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void shouldHandleVeryLongToken() throws ServletException, IOException {
        String longToken = "a".repeat(1000);
        Cookie[] cookies = {
            new Cookie("auth_token", longToken)
        };
        when(request.getCookies()).thenReturn(cookies);
        when(jwtService.extractUsername(longToken)).thenReturn("testuser");
        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(userDetails);
        when(jwtService.isTokenValid(longToken, userDetails)).thenReturn(true);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
    }
}
