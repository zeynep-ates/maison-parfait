package com.zeynepates.maisonparfait.backend.identity;

import com.zeynepates.maisonparfait.backend.modules.user.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

/**
 * Fully stateless - an access token carries only what's needed to authorize
 * a request (user id, role). No mutable profile data (email, verification
 * status, etc.) goes in claims, so a stale cached token can never disagree
 * with the database about anything except authorization itself, which is
 * exactly what its short TTL already bounds.
 */
@Service
public class TokenService {

    private final Key key;

    @Getter
    private final long accessTokenTtlSeconds;

    public TokenService(
            @Value("${security.jwt.secret}") String secret,
            @Value("${security.jwt.access-token-ttl-seconds:900}") long accessTokenTtlSeconds
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.accessTokenTtlSeconds = accessTokenTtlSeconds;
    }

    public String generateAccessToken(Long userId, UserRole role) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(userId.toString())
                .claim("role", role.name())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(accessTokenTtlSeconds)))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Optional<AuthenticatedPrincipal> parse(String token) {
        try {
            Claims claims = Jwts.parserBuilder().setSigningKey(key).build()
                    .parseClaimsJws(token)
                    .getBody();

            Long userId = Long.valueOf(claims.getSubject());
            String role = claims.get("role", String.class);
            if (role == null) {
                return Optional.empty();
            }
            return Optional.of(new AuthenticatedPrincipal(userId, role));
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public record AuthenticatedPrincipal(Long userId, String role) {
    }
}
