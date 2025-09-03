package co.com.authservice.jwt;

import co.com.authservice.model.user.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class JWTTokenServiceImpl {
    
    private final SecretKey key;
    private final long expirationTime;
    
    public JWTTokenServiceImpl(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration:86400}") long expirationTime) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.expirationTime = expirationTime;
    }
    
    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("email", user.getEmail());
        claims.put("role", user.getRole().getName());
        
        Date now = new Date();
        Date expiration = new Date(now.getTime() + expirationTime * 1000);
        
        return Jwts.builder()
                .claims(claims)
                .subject(user.getEmail())
                .issuedAt(now)
                .expiration(expiration)
                .signWith(key)
                .compact();
    }
    
    public boolean isValidToken(String token) {
        try {
            Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("Invalid token: {}", e.getMessage());
            return false;
        }
    }
    
    public Long extractUserId(String token) {
        Claims claims = getClaims(token);
        return claims.get("userId", Long.class);
    }
    
    public Long getExpirationTime() {
        return expirationTime;
    }
    
    public String extractEmail(String token) {
        return getClaims(token).getSubject();
    }
    
    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
