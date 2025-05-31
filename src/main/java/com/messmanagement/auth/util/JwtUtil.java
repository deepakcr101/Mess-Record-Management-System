package com.messmanagement.auth.util;

import com.messmanagement.user.entity.User; // Assuming UserDetails might be an interface User implements
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails; // Spring Security's UserDetails
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    @Value("${jwt.secret}") // Load secret from application.properties or environment variable
    private String secretKeyString;

    @Value("${jwt.access.token.expiration.ms}") // e.g., 3600000 for 1 hour
    private long accessTokenExpirationMs;

    @Value("${jwt.refresh.token.expiration.ms}") // e.g., 86400000 for 1 day
    private long refreshTokenExpirationMs;

    private Key getSigningKey() {
        // Ensure the secret key is strong enough for HS256.
        // For HS256, the key size should be at least 256 bits (32 bytes).
        // If your secretKeyString is shorter, this might lead to weak keys.
        // Consider generating a secure key and storing it.
        byte[] keyBytes = secretKeyString.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token).getBody();
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public String generateAccessToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        // You can add custom claims here, e.g., roles
        // com.messmanagement.user.entity.User customUser = (com.messmanagement.user.entity.User) userDetails;
        // claims.put("userId", customUser.getUserId());
        // claims.put("role", customUser.getRole().name());
        return createToken(claims, userDetails.getUsername(), accessTokenExpirationMs);
    }

    public String generateRefreshToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        // Refresh tokens typically only contain subject and expiration
        return createToken(claims, userDetails.getUsername(), refreshTokenExpirationMs);
    }

    private String createToken(Map<String, Object> claims, String subject, long expirationTimeMs) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject) // Typically the username (email in our case)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationTimeMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    // Overload for validation without UserDetails (e.g. for refresh token before loading user)
    public Boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token);
            return !isTokenExpired(token);
        } catch (Exception e) { // Catches various JWT exceptions like ExpiredJwtException, SignatureException etc.
            return false;
        }
    }
}