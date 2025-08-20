package com.example.taskmanager.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

/**
 * Utility class for handling JWT token generation, validation, and parsing.
 */
@Component
public class JwtUtil {

    // Secret key used to sign the JWT, injected from application properties
    @Value("${jwt.secret}")
    private String secretKeyString;

    // Token expiration time in milliseconds, injected from application properties
    @Value("${jwt.expiration}")
    private long expirationTime;

    // SecretKey instance generated from the string key
    private SecretKey key;

    /**
     * Initializes the SecretKey after dependency injection is complete.
     * This method converts the string secret into a HMAC-SHA key.
     */
    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secretKeyString.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generates a signed JWT token with the specified claims and subject (username).
     *
     * @param claims   Additional claims to include in the token payload
     * @param username The subject of the token (usually the username)
     * @return A signed JWT token string
     */
    public String generateToken(Map<String, Object> claims, String username) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Validates the JWT token by checking the signature and expiration time.
     *
     * @param token The JWT token string to validate
     * @return True if the token is valid; false otherwise
     */
    public boolean isTokenValid(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    /**
     * Extracts the username (subject) from the given JWT token.
     *
     * @param token The JWT token
     * @return The username contained in the token's subject field
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extracts the user's role from the JWT token's claims.
     *
     * @param token The JWT token
     * @return The role value from the token's custom claims
     */
    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    /**
     * Generic method to extract a specific claim using a resolver function.
     *
     * @param token           The JWT token
     * @param claimsResolver  A function that defines how to extract a claim from the Claims object
     * @param <T>             The type of the claim value
     * @return The extracted claim
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extracts all claims from the JWT token.
     *
     * @param token The JWT token
     * @return The Claims object containing all token data
     */
    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .getBody();
    }
}
