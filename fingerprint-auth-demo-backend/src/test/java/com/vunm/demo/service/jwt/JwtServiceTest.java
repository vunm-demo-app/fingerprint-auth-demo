package com.vunm.demo.service.jwt;

import com.vunm.demo.domain.service.jwt.JwtService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    private JwtService jwtService;

    private static final String TEST_SECRET = "testSecretKey1234567890123456789012345678901234567890";
    private static final Long TEST_EXPIRATION = 300000L; // 5 minutes
    private static final String TEST_VISITOR_ID = "testVisitor123";

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(TEST_SECRET, TEST_EXPIRATION);
    }

    @Test
    void generateToken_WithValidVisitorId_ShouldGenerateValidToken() {
        // When
        String token = jwtService.generateToken(TEST_VISITOR_ID);

        // Then
        assertNotNull(token);
        assertTrue(jwtService.validateToken(token));
        assertEquals(TEST_VISITOR_ID, jwtService.getVisitorIdFromToken(token));
    }

    @Test
    void generateToken_WithNullVisitorId_ShouldThrowException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            jwtService.generateToken(null);
        });
    }

    @Test
    void generateToken_WithEmptyVisitorId_ShouldThrowException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            jwtService.generateToken("");
        });
    }

    @Test
    void validateToken_WithValidToken_ShouldReturnTrue() {
        // Given
        String token = jwtService.generateToken(TEST_VISITOR_ID);

        // When
        boolean isValid = jwtService.validateToken(token);

        // Then
        assertTrue(isValid);
    }

    @Test
    void validateToken_WithInvalidToken_ShouldReturnFalse() {
        // Given
        String invalidToken = "invalid.token.here";

        // When
        boolean isValid = jwtService.validateToken(invalidToken);

        // Then
        assertFalse(isValid);
    }

    @Test
    void validateToken_WithExpiredToken_ShouldReturnFalse() {
        // Given
        ReflectionTestUtils.setField(jwtService, "expiration", 0L); // Set expiration to 0
        String expiredToken = jwtService.generateToken(TEST_VISITOR_ID);
        ReflectionTestUtils.setField(jwtService, "expiration", TEST_EXPIRATION); // Reset expiration

        // When
        boolean isValid = jwtService.validateToken(expiredToken);

        // Then
        assertFalse(isValid);
    }

    @Test
    void getVisitorIdFromToken_WithValidToken_ShouldReturnVisitorId() {
        // Given
        String token = jwtService.generateToken(TEST_VISITOR_ID);

        // When
        String visitorId = jwtService.getVisitorIdFromToken(token);

        // Then
        assertEquals(TEST_VISITOR_ID, visitorId);
    }

    @Test
    void getVisitorIdFromToken_WithInvalidToken_ShouldThrowException() {
        // Given
        String invalidToken = "invalid.token.here";

        // When & Then
        assertThrows(Exception.class, () -> {
            jwtService.getVisitorIdFromToken(invalidToken);
        });
    }
} 