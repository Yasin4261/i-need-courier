package com.api.demo.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT Token Provider for generating and validating JWT tokens.
 */
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret:mySecretKeyForJWTTokenGenerationAndValidation1234567890}")
    private String jwtSecret;

    @Value("${jwt.expiration-hours:24}")
    private long jwtExpirationHours;

    /**
     * Generate JWT token for courier.
     *
     * @param courierId Courier ID
     * @param email Courier email
     * @return JWT token string
     */
    public String generateToken(Long courierId, String email) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("courierId", courierId);
        claims.put("email", email);

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationHours * 60 * 60 * 1000);

        Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes());

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(email)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS384)
                .compact();
    }

    /**
     * Get courier ID from JWT token.
     *
     * @param token JWT token
     * @return Courier ID
     */
    public Long getCourierIdFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return claims.get("courierId", Long.class);
    }

    /**
     * Get email from JWT token.
     *
     * @param token JWT token
     * @return Email address
     */
    public String getEmailFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return claims.getSubject();
    }

    /**
     * Validate JWT token.
     *
     * @param token JWT token
     * @return true if valid, false otherwise
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = getAllClaimsFromToken(token);
            return !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get all claims from JWT token.
     *
     * @param token JWT token
     * @return Claims
     */
    private Claims getAllClaimsFromToken(String token) {
        Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        return Jwts.parser()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}

