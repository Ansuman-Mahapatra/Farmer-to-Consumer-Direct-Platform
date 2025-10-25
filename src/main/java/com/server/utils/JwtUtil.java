package com.server.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    private SecretKey getSigningKey() {
        // Ensure the secret is valid length for HS256 (at least 256 bits / 32 bytes)
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            // Pad if too short
            byte[] paddedKey = new byte[32];
            System.arraycopy(keyBytes, 0, paddedKey, 0, Math.min(keyBytes.length, 32));
            keyBytes = paddedKey;
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(String username, String role) {
        return Jwts.builder()
                .subject(username)                    // ✅ replaced setSubject() → subject()
                .claim("role", role)
                .issuedAt(new Date())                 // ✅ replaced setIssuedAt() → issuedAt()
                .expiration(new Date(System.currentTimeMillis() + expiration)) // ✅ replaced setExpiration() → expiration()
                .signWith(getSigningKey())            // ✅ new API version auto-selects HS256
                .compact();
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()                          // ✅ new parser (no builder)
                .verifyWith(getSigningKey())           // ✅ verify signature
                .build()
                .parseSignedClaims(token)              // ✅ replaces parseClaimsJws()
                .getPayload();                         // ✅ replaces getBody()
    }

    public boolean validateToken(String token) {
        try {
            extractAllClaims(token);
            return true;
        } catch (Exception e) {
            // You can log this exception if needed
            return false;
        }
    }
}
