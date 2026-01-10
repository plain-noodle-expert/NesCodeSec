package com.healthcare.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * Service class for JWT Token operations
 */
@Service
public class TokenService {

    @Value("${jwt.secret:mySecretKey123456789012345678901234567890}")
    private String jwtSecret;

    @Value("${jwt.expiration:86400000}")
    private Long jwtExpiration;

    /**
     * Method to generate a JWT token using the user's email
     * 
     * @param email User's email
     * @return Generated JWT token
     */
    public String generateToken(String email) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);

        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Method to return the signing key using the configured secret
     * 
     * @return SecretKey for JWT signing
     */
    public SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }
}