package com.github.pi_tracking.backend.service;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JWTServiceTest {

    private JWTService jwtService;
    private static final String SECRET_KEY = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private static final long EXPIRATION_TIME = 86400000; // 24 hours

    @BeforeEach
    void setUp() {
        jwtService = new JWTService();
        ReflectionTestUtils.setField(jwtService, "secretKey", SECRET_KEY);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", EXPIRATION_TIME);
    }

    @Test
    void generateToken_ShouldGenerateValidToken() {
        UserDetails userDetails = User.builder()
                .username("testuser")
                .password("password")
                .authorities("ROLE_USER")
                .build();

        String token = jwtService.generateToken(userDetails);

        assertNotNull(token);
        assertTrue(token.split("\\.").length == 3); // JWT has 3 parts
    }

    @Test
    void generateToken_WithExtraClaims_ShouldIncludeClaims() {
        UserDetails userDetails = User.builder()
                .username("testuser")
                .password("password")
                .authorities("ROLE_USER")
                .build();
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("userBadge", "123");

        String token = jwtService.generateToken(extraClaims, userDetails);

        assertNotNull(token);
        String extractedBadge = jwtService.extractUserBadge(token);
        assertEquals("123", extractedBadge);
    }

    @Test
    void extractUsername_ShouldExtractCorrectUsername() {
        UserDetails userDetails = User.builder()
                .username("testuser")
                .password("password")
                .authorities("ROLE_USER")
                .build();
        String token = jwtService.generateToken(userDetails);

        String username = jwtService.extractUsername(token);

        assertEquals("testuser", username);
    }

    @Test
    void isTokenValid_WithValidToken_ShouldReturnTrue() {
        UserDetails userDetails = User.builder()
                .username("testuser")
                .password("password")
                .authorities("ROLE_USER")
                .build();
        String token = jwtService.generateToken(userDetails);

        boolean isValid = jwtService.isTokenValid(token, userDetails);

        assertTrue(isValid);
    }

    @Test
    void isTokenValid_WithInvalidUsername_ShouldReturnFalse() {
        UserDetails userDetails = User.builder()
                .username("testuser")
                .password("password")
                .authorities("ROLE_USER")
                .build();
        String token = jwtService.generateToken(userDetails);

        UserDetails differentUser = User.builder()
                .username("differentuser")
                .password("password")
                .authorities("ROLE_USER")
                .build();

        boolean isValid = jwtService.isTokenValid(token, differentUser);

        assertFalse(isValid);
    }

    @Test
    void extractClaim_ShouldExtractCorrectClaim() {
        UserDetails userDetails = User.builder()
                .username("testuser")
                .password("password")
                .authorities("ROLE_USER")
                .build();
        String token = jwtService.generateToken(userDetails);

        String username = jwtService.extractClaim(token, Claims::getSubject);

        assertEquals("testuser", username);
    }
} 