package com.zeynepates.maisonparfait.backend.identity;

import com.zeynepates.maisonparfait.backend.common.exception.NotFoundException;
import com.zeynepates.maisonparfait.backend.identity.dto.SessionResponse;
import com.zeynepates.maisonparfait.backend.identity.mapper.SessionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * A user-facing view over the same refresh_tokens table RefreshTokenService
 * manages internally for the auth flow itself - one row is one session.
 */
@Service
@RequiredArgsConstructor
public class SessionService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenGenerator tokenGenerator;
    private final SessionMapper sessionMapper;

    @Transactional(readOnly = true)
    public List<SessionResponse> listActiveSessions(Long userId, String currentRawRefreshToken) {
        String currentHash = hashOrNull(currentRawRefreshToken);

        return refreshTokenRepository
                .findAllByUser_IdAndRevokedAtIsNullAndExpiresAtAfter(userId, OffsetDateTime.now())
                .stream()
                .map(token -> sessionMapper.toResponse(token, token.getTokenHash().equals(currentHash)))
                .toList();
    }

    @Transactional
    public void revokeSession(Long userId, Long sessionId) {
        RefreshToken token = refreshTokenRepository.findByIdAndUser_Id(sessionId, userId)
                .orElseThrow(() -> new NotFoundException("Session not found: " + sessionId));
        token.setRevokedAt(OffsetDateTime.now());
    }

    @Transactional
    public void revokeAllExceptCurrent(Long userId, String currentRawRefreshToken) {
        String currentHash = hashOrNull(currentRawRefreshToken);
        OffsetDateTime now = OffsetDateTime.now();

        refreshTokenRepository.findAllByUser_IdAndRevokedAtIsNullAndExpiresAtAfter(userId, now).stream()
                .filter(token -> !token.getTokenHash().equals(currentHash))
                .forEach(token -> token.setRevokedAt(now));
    }

    private String hashOrNull(String rawToken) {
        return rawToken == null ? null : tokenGenerator.hash(rawToken);
    }
}
