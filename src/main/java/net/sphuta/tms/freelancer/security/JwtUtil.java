package net.sphuta.tms.freelancer.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.Set;

@Component
public class JwtUtil {
    @Value("${jwt.secret}")
    private String secret;
    @Value("${jwt.expirationMs}")
    private long expirationMs;

    private Key getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateToken(String subject, Set<String> roles) {
        var now = System.currentTimeMillis();
        return Jwts.builder()
                .setSubject(subject)
                .claim("roles", roles)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + expirationMs))
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public Jws<Claims> parse(String token) {
        return Jwts.parserBuilder().setSigningKey(getKey()).build().parseClaimsJws(token);
    }

    /**
     * Extracts the expiration time from the given JWT as an Instant.
     * This method will throw the same exceptions as {@link #parse(String)} if the token is invalid.
     *
     * @param token the JWT (compact serialization)
     * @return expiration Instant
     */
    public Instant getExpiryInstant(String token) {
        Jws<Claims> jws = parse(token);
        Date exp = jws.getBody().getExpiration();
        return exp.toInstant();
    }
}