package com.vunm.demo.domain.service.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {
    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);
    private final SecretKey signingKey;
    private final long expiration;

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration}") long expiration) {
        // Create a key from the secret that's at least 256 bits (32 bytes) for HS256
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            // Pad the key to 32 bytes if it's too short
            byte[] paddedKey = new byte[32];
            System.arraycopy(keyBytes, 0, paddedKey, 0, Math.min(keyBytes.length, 32));
            this.signingKey = Keys.hmacShaKeyFor(paddedKey);
        } else {
            this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        }
        this.expiration = expiration;
    }

    public String generateToken(String visitorId) {
        if (visitorId == null || visitorId.trim().isEmpty()) {
            throw new IllegalArgumentException("Visitor ID cannot be null or empty");
        }

        try {
            Date now = new Date();
            Date expiryDate = new Date(now.getTime() + expiration);

            return Jwts.builder()
                    .subject(visitorId)
                    .issuedAt(now)
                    .expiration(expiryDate)
                    .signWith(signingKey, Jwts.SIG.HS256)
                    .compact();
        } catch (Exception e) {
            logger.error("Error generating JWT token for visitorId: {}", visitorId, e);
            throw new JwtException("Failed to generate token", e);
        }
    }

    public String getVisitorIdFromToken(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.getSubject();
        } catch (Exception e) {
            logger.error("Error extracting visitorId from token", e);
            throw new JwtException("Failed to extract visitorId from token", e);
        }
    }

    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    private Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}