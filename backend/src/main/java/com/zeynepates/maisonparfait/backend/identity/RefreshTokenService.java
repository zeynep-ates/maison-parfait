package com.zeynepates.maisonparfait.backend.identity;

import com.zeynepates.maisonparfait.backend.common.exception.UnauthorizedException;
import com.zeynepates.maisonparfait.backend.modules.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

/**
 * The refresh_tokens table is the only server-side session state in this
 * module - access tokens are stateless JWTs validated by signature alone.
 * One row = one session/device.
 */
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final String INVALID_REFRESH_TOKEN_MESSAGE = "Invalid or expired refresh token";

    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenGenerator tokenGenerator;

    @Value("${security.refresh-token.ttl-seconds:604800}")
    private long defaultTtlSeconds;

    @Value("${security.refresh-token.remember-me-ttl-seconds:2592000}")
    private long rememberMeTtlSeconds;

    @Transactional
    public IssuedRefreshToken issue(User user, String userAgent, String ipAddress, boolean rememberMe) {
        String rawToken = tokenGenerator.generateRawToken();
        OffsetDateTime expiresAt = OffsetDateTime.now().plusSeconds(ttlFor(rememberMe));

        RefreshToken token = new RefreshToken();
        token.setUser(user);
        token.setTokenHash(tokenGenerator.hash(rawToken));
        token.setExpiresAt(expiresAt);
        token.setRememberMe(rememberMe);
        token.setUserAgent(userAgent);
        token.setIpAddress(ipAddress);
        refreshTokenRepository.save(token);

        return new IssuedRefreshToken(rawToken, expiresAt);
    }

    /**
     * Rotates a refresh token: the presented one is revoked and superseded
     * by a brand-new one with the same rememberMe/TTL policy. If the
     * presented token was already revoked - meaning it's being replayed,
     * since a legitimate client only ever holds the newest token it was
     * given - the entire chain from it forward is revoked as a theft
     * response, not just this one request declined.
     */
    @Transactional
    public RotationResult rotate(String rawToken, String userAgent, String ipAddress) {
        String hash = tokenGenerator.hash(rawToken);
        RefreshToken existing = refreshTokenRepository.findByTokenHash(hash)
                .orElseThrow(() -> new UnauthorizedException(INVALID_REFRESH_TOKEN_MESSAGE));

        if (existing.getRevokedAt() != null) {
            revokeChainForward(existing);
            throw new UnauthorizedException(INVALID_REFRESH_TOKEN_MESSAGE);
        }
        if (existing.getExpiresAt().isBefore(OffsetDateTime.now())) {
            throw new UnauthorizedException(INVALID_REFRESH_TOKEN_MESSAGE);
        }

        boolean rememberMe = Boolean.TRUE.equals(existing.getRememberMe());
        String newRawToken = tokenGenerator.generateRawToken();
        String newHash = tokenGenerator.hash(newRawToken);
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime newExpiresAt = now.plusSeconds(ttlFor(rememberMe));

        existing.setRevokedAt(now);
        existing.setReplacedByTokenHash(newHash);
        existing.setLastUsedAt(now);

        RefreshToken next = new RefreshToken();
        next.setUser(existing.getUser());
        next.setTokenHash(newHash);
        next.setExpiresAt(newExpiresAt);
        next.setRememberMe(rememberMe);
        next.setUserAgent(userAgent);
        next.setIpAddress(ipAddress);
        refreshTokenRepository.save(next);

        return new RotationResult(existing.getUser(), new IssuedRefreshToken(newRawToken, newExpiresAt));
    }

    @Transactional
    public void revoke(String rawToken) {
        refreshTokenRepository.findByTokenHash(tokenGenerator.hash(rawToken))
                .filter(token -> token.getRevokedAt() == null)
                .ifPresent(token -> token.setRevokedAt(OffsetDateTime.now()));
    }

    private void revokeChainForward(RefreshToken start) {
        OffsetDateTime now = OffsetDateTime.now();
        RefreshToken current = start;

        while (current != null) {
            if (current.getRevokedAt() == null) {
                current.setRevokedAt(now);
            }
            current = current.getReplacedByTokenHash() == null
                    ? null
                    : refreshTokenRepository.findByTokenHash(current.getReplacedByTokenHash()).orElse(null);
        }
    }

    private long ttlFor(boolean rememberMe) {
        return rememberMe ? rememberMeTtlSeconds : defaultTtlSeconds;
    }

    public record IssuedRefreshToken(String rawToken, OffsetDateTime expiresAt) {
    }

    public record RotationResult(User user, IssuedRefreshToken refreshToken) {
    }
}
