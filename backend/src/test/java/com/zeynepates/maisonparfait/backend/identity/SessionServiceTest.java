package com.zeynepates.maisonparfait.backend.identity;

import com.zeynepates.maisonparfait.backend.common.exception.NotFoundException;
import com.zeynepates.maisonparfait.backend.identity.dto.SessionResponse;
import com.zeynepates.maisonparfait.backend.identity.mapper.SessionMapper;
import com.zeynepates.maisonparfait.backend.modules.user.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.InjectMocks;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SessionServiceTest {

    @Mock
    RefreshTokenRepository refreshTokenRepository;

    @Mock
    TokenGenerator tokenGenerator;

    @Mock
    SessionMapper sessionMapper;

    @InjectMocks
    SessionService sessionService;

    private RefreshToken tokenWithHash(String hash) {
        RefreshToken token = new RefreshToken();
        User user = new User();
        user.setId(1L);
        token.setUser(user);
        token.setTokenHash(hash);
        token.setExpiresAt(OffsetDateTime.now().plusDays(1));
        return token;
    }

    @Test
    void listActiveSessionsFlagsTheOneMatchingTheCurrentCookie() {
        RefreshToken current = tokenWithHash("hash-current");
        RefreshToken other = tokenWithHash("hash-other");

        when(tokenGenerator.hash("raw-current")).thenReturn("hash-current");
        when(refreshTokenRepository.findAllByUser_IdAndRevokedAtIsNullAndExpiresAtAfter(eq(1L), any()))
                .thenReturn(List.of(current, other));
        when(sessionMapper.toResponse(eq(current), eq(true)))
                .thenReturn(new SessionResponse(1L, "UA", "127.0.0.1", OffsetDateTime.now(), null, true));
        when(sessionMapper.toResponse(eq(other), eq(false)))
                .thenReturn(new SessionResponse(2L, "UA2", "127.0.0.2", OffsetDateTime.now(), null, false));

        List<SessionResponse> sessions = sessionService.listActiveSessions(1L, "raw-current");

        assertThat(sessions).hasSize(2);
        assertThat(sessions).anySatisfy(s -> assertThat(s.current()).isTrue());
        assertThat(sessions).filteredOn(SessionResponse::current).hasSize(1);
    }

    @Test
    void listActiveSessionsFlagsNoneWhenNoCookiePresent() {
        RefreshToken token = tokenWithHash("hash-1");
        when(refreshTokenRepository.findAllByUser_IdAndRevokedAtIsNullAndExpiresAtAfter(eq(1L), any()))
                .thenReturn(List.of(token));
        when(sessionMapper.toResponse(eq(token), eq(false)))
                .thenReturn(new SessionResponse(1L, "UA", "127.0.0.1", OffsetDateTime.now(), null, false));

        List<SessionResponse> sessions = sessionService.listActiveSessions(1L, null);

        assertThat(sessions).extracting(SessionResponse::current).containsExactly(false);
    }

    @Test
    void revokeSessionMarksItRevokedWhenOwnedByCaller() {
        RefreshToken token = tokenWithHash("hash-1");
        when(refreshTokenRepository.findByIdAndUser_Id(5L, 1L)).thenReturn(Optional.of(token));

        sessionService.revokeSession(1L, 5L);

        assertThat(token.getRevokedAt()).isNotNull();
    }

    @Test
    void revokeSessionThrowsNotFoundForForeignOrUnknownSession() {
        when(refreshTokenRepository.findByIdAndUser_Id(5L, 1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> sessionService.revokeSession(1L, 5L));
    }

    @Test
    void revokeAllExceptCurrentLeavesTheCurrentSessionActive() {
        RefreshToken current = tokenWithHash("hash-current");
        RefreshToken other1 = tokenWithHash("hash-other-1");
        RefreshToken other2 = tokenWithHash("hash-other-2");

        when(tokenGenerator.hash("raw-current")).thenReturn("hash-current");
        when(refreshTokenRepository.findAllByUser_IdAndRevokedAtIsNullAndExpiresAtAfter(eq(1L), any()))
                .thenReturn(List.of(current, other1, other2));

        sessionService.revokeAllExceptCurrent(1L, "raw-current");

        assertThat(current.getRevokedAt()).isNull();
        assertThat(other1.getRevokedAt()).isNotNull();
        assertThat(other2.getRevokedAt()).isNotNull();
    }
}
