package net.sphuta.tms.freelancer.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.Set;

/**
 * Unified JWT utility.
 * - Supports auth tokens (subject + roles)
 * - Supports ack tokens (with "nid" claim for notification id)
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expirationMs}")
    private long expirationMs;

    @Value("${app.notifications.token-ttl-seconds:86400}")
    private long ackTokenTtlSeconds;

    private Key getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * Generate a JWT for authentication with subject + roles.
     */
    public String generateToken(String subject, Set<String> roles) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setSubject(subject)
                .claim("roles", roles)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + expirationMs))
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Parse a JWT (throws JwtException if invalid/expired).
     */
    public Jws<Claims> parse(String token) {
        return Jwts.parserBuilder().setSigningKey(getKey()).build().parseClaimsJws(token);
    }

    /**
     * Extracts the expiration time from a JWT as an Instant.
     */
    public Instant getExpiryInstant(String token) {
        Jws<Claims> jws = parse(token);
        Date exp = jws.getBody().getExpiration();
        return exp.toInstant();
    }

    // -------------------------------------------------------------
    // 🔔 Ack token support for Notifications
    // -------------------------------------------------------------

    /**
     * Generate an ack token for a notification id.
     */
    public String createAckToken(Long notificationId) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setClaims(Map.of("nid", notificationId))
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(ackTokenTtlSeconds)))
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Extract notification id (nid) from an ack token.
     */
    public Long getNotificationIdFromAckToken(String token) throws JwtException {
        Jws<Claims> jws = parse(token);
        Object nid = jws.getBody().get("nid");
        if (nid instanceof Integer i) return i.longValue();
        if (nid instanceof Long l) return l;
        if (nid instanceof String s) return Long.valueOf(s);
        throw new JwtException("Invalid nid in ack token");
    }

    /**
     * Backwards-compatible alias for older code that called getNotificationIdFromToken(...)
     * Keeps existing code working without change.
     */
    public Long getNotificationIdFromToken(String token) throws JwtException {
        return getNotificationIdFromAckToken(token);
    }
}
