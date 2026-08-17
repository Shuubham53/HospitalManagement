package com.Application.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class AuthUtil {

    @Value("${jwt.secretKey}")
    private String secretKey;

    private static final long ACCESS_TOKEN_EXPIRATION = 1000L * 60 * 20;
    private static final long REFRESH_TOKEN_EXPIRATION = 1000L * 60 * 60 * 24 * 7;

    public SecretKey getSecretKey(){
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(String username, Long id) {
        return Jwts.builder()
                .claim("userId", id)
                .claim("type", "access")
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(
                        System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION
                ))
                .signWith(getSecretKey())
                .compact();
    }
    public String generateRefreshToken(String username, Long id) {
        return Jwts.builder()
                .claim("userId", id)
                .claim("type", "refresh")
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(
                        System.currentTimeMillis() + REFRESH_TOKEN_EXPIRATION
                ))
                .signWith(getSecretKey())
                .compact();
    }

    public String extractUsernameFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.getSubject();
    }

    public boolean isRefreshToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return "refresh".equals(claims.get("type", String.class));
    }
}
