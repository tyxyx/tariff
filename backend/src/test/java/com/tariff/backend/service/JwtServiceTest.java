package com.tariff.backend.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.Key;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private String testSecretKey;
    private long testExpiration = 3600000; // 1 hour

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        
        // Generate a valid base64-encoded secret key (256 bits for HS256)
        byte[] keyBytes = new byte[32]; // 256 bits
        for (int i = 0; i < keyBytes.length; i++) {
            keyBytes[i] = (byte) i;
        }
        testSecretKey = Base64.getEncoder().encodeToString(keyBytes);
        
        // Set the secret key and expiration using reflection
        ReflectionTestUtils.setField(jwtService, "secretKey", testSecretKey);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", testExpiration);
    }

    @Test
    void generateToken_shouldCreateValidToken() {
        UserDetails userDetails = User.builder()
                .username("test@example.com")
                .password("password")
                .authorities(new ArrayList<>())
                .build();

        String token = jwtService.generateToken(userDetails);

        assertNotNull(token);
        assertFalse(token.isEmpty());
        
        // Verify the token contains the username
        String extractedUsername = jwtService.extractUsername(token);
        assertEquals("test@example.com", extractedUsername);
    }

    @Test
    void generateToken_withExtraClaims_shouldIncludeClaimsInToken() {
        UserDetails userDetails = User.builder()
                .username("admin@example.com")
                .password("password")
                .authorities(new ArrayList<>())
                .build();
        
        HashMap<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", "ADMIN");
        extraClaims.put("userId", "12345");

        String token = jwtService.generateToken(extraClaims, userDetails);

        assertNotNull(token);
        
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        
        assertEquals("ADMIN", claims.get("role"));
        assertEquals("12345", claims.get("userId"));
        assertEquals("admin@example.com", claims.getSubject());
    }

    @Test
    void extractUsername_shouldReturnCorrectUsername() {
        UserDetails userDetails = User.builder()
                .username("user@test.com")
                .password("password")
                .authorities(new ArrayList<>())
                .build();
        String token = jwtService.generateToken(userDetails);

        String username = jwtService.extractUsername(token);

        assertEquals("user@test.com", username);
    }

    @Test
    void isTokenValid_withValidToken_shouldReturnTrue() {
        UserDetails userDetails = User.builder()
                .username("valid@example.com")
                .password("password")
                .authorities(new ArrayList<>())
                .build();
        String token = jwtService.generateToken(userDetails);

        boolean isValid = jwtService.isTokenValid(token, userDetails);

        assertTrue(isValid);
    }

    @Test
    void isTokenValid_withDifferentUser_shouldReturnFalse() {
        UserDetails originalUser = User.builder()
                .username("user1@example.com")
                .password("password")
                .authorities(new ArrayList<>())
                .build();
        
        UserDetails differentUser = User.builder()
                .username("user2@example.com")
                .password("password")
                .authorities(new ArrayList<>())
                .build();
        
        String token = jwtService.generateToken(originalUser);

        boolean isValid = jwtService.isTokenValid(token, differentUser);

        assertFalse(isValid);
    }

    @Test
    void isTokenValid_withExpiredToken_shouldReturnFalse() {
        UserDetails userDetails = User.builder()
                .username("expired@example.com")
                .password("password")
                .authorities(new ArrayList<>())
                .build();
        
        String expiredToken = Jwts.builder()
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis() - 7200000)) // 2 hours ago
                .setExpiration(new Date(System.currentTimeMillis() - 3600000)) // 1 hour ago
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();

        assertThrows(ExpiredJwtException.class, () -> {
            jwtService.isTokenValid(expiredToken, userDetails);
        });
    }

    @Test
    void extractClaim_shouldExtractSpecificClaim() {
        UserDetails userDetails = User.builder()
                .username("claim@example.com")
                .password("password")
                .authorities(new ArrayList<>())
                .build();
        String token = jwtService.generateToken(userDetails);

        Date issuedAt = jwtService.extractClaim(token, Claims::getIssuedAt);
        Date expiration = jwtService.extractClaim(token, Claims::getExpiration);

        assertNotNull(issuedAt);
        assertNotNull(expiration);
        assertTrue(expiration.after(issuedAt));
        
        // Verify expiration is approximately 1 hour from issued time
        long timeDiff = expiration.getTime() - issuedAt.getTime();
        assertEquals(testExpiration, timeDiff, 1000); // Allow 1 second tolerance
    }

    @Test
    void getExpirationTime_shouldReturnConfiguredExpiration() {
        long expiration = jwtService.getExpirationTime();

        assertEquals(testExpiration, expiration);
    }

    @Test
    void generateToken_shouldSetCorrectExpiration() {
        UserDetails userDetails = User.builder()
                .username("expiration@example.com")
                .password("password")
                .authorities(new ArrayList<>())
                .build();

        String token = jwtService.generateToken(userDetails);
        Date expiration = jwtService.extractClaim(token, Claims::getExpiration);
        Date issuedAt = jwtService.extractClaim(token, Claims::getIssuedAt);

        long actualExpiration = expiration.getTime() - issuedAt.getTime();
        assertEquals(testExpiration, actualExpiration, 1000); // Allow 1 second tolerance
    }

    @Test
    void extractUsername_fromTokenWithMultipleClaims_shouldReturnCorrectUsername() {
        UserDetails userDetails = User.builder()
                .username("multi@example.com")
                .password("password")
                .authorities(new ArrayList<>())
                .build();
        
        HashMap<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", "USER");
        extraClaims.put("department", "Engineering");
        
        String token = jwtService.generateToken(extraClaims, userDetails);

        String username = jwtService.extractUsername(token);

        assertEquals("multi@example.com", username);
    }

    // Helper method to get the signing key for verification
    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(testSecretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
