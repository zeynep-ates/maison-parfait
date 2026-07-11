package com.zeynepates.maisonparfait.backend.identity;

import com.zeynepates.maisonparfait.backend.common.exception.UnauthorizedException;
import com.zeynepates.maisonparfait.backend.modules.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    private static final long DEFAULT_TTL_SECONDS = 604800L;
    private static final long REMEMBER_ME_TTL_SECONDS = 2592000L;

    @Mock
    RefreshTokenRepository refreshTokenRepository;

    @Mock
    TokenGenerator tokenGenerator;

    @InjectMocks
    RefreshTokenService refreshTokenService;

    User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        ReflectionTestUtils.setField(refreshTokenService, "defaultTtlSeconds", DEFAULT_TTL_SECONDS);
        ReflectionTestUtils.setField(refreshTokenService, "rememberMeTtlSeconds", REMEMBER_ME_TTL_SECONDS);
    }

    @Test
    void issueUsesStandardTtlWhenRememberMeIsFalse() {
        when(tokenGenerator.generateRawToken()).thenReturn("raw-1");
        when(tokenGenerator.hash("raw-1")).thenReturn("hash-1");

        RefreshTokenService.IssuedRefreshToken issued = refreshTokenService.issue(user, "UA", "127.0.0.1", false);

        ArgumentCaptor<RefreshToken> saved = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(saved.capture());
        assertThat(saved.getValue().getTokenHash()).isEqualTo("hash-1");
        assertThat(saved.getValue().getRememberMe()).isFalse();
        assertThat(issued.rawToken()).isEqualTo("raw-1");
        assertThat(issued.expiresAt()).isAfter(OffsetDateTime.now().plusDays(6));
        assertThat(issued.expiresAt()).isBefore(OffsetDateTime.now().plusDays(8));
    }

    @Test
    void issueUsesRememberMeTtlWhenRequested() {
        when(tokenGenerator.generateRawToken()).thenReturn("raw-1");
        when(tokenGenerator.hash("raw-1")).thenReturn("hash-1");

        RefreshTokenService.IssuedRefreshToken issued = refreshTokenService.issue(user, "UA", "127.0.0.1", true);

        assertThat(issued.expiresAt()).isAfter(OffsetDateTime.now().plusDays(29));
        assertThat(issued.expiresAt()).isBefore(OffsetDateTime.now().plusDays(31));
    }

    @Test
    void rotateRevokesOldTokenAndPreservesRememberMe() {
        RefreshToken existing = new RefreshToken();
        existing.setUser(user);
        existing.setTokenHash("hash-old");
        existing.setExpiresAt(OffsetDateTime.now().plusDays(1));
        existing.setRememberMe(true);

        when(tokenGenerator.hash("raw-old")).thenReturn("hash-old");
        when(refreshTokenRepository.findByTokenHash("hash-old")).thenReturn(Optional.of(existing));
        when(tokenGenerator.generateRawToken()).thenReturn("raw-new");
        when(tokenGenerator.hash("raw-new")).thenReturn("hash-new");

        RefreshTokenService.RotationResult result = refreshTokenService.rotate("raw-old", "UA", "127.0.0.1");

        assertThat(existing.getRevokedAt()).isNotNull();
        assertThat(existing.getReplacedByTokenHash()).isEqualTo("hash-new");
        assertThat(result.user()).isEqualTo(user);
        assertThat(result.refreshToken().rawToken()).isEqualTo("raw-new");
        // remember-me survives rotation, otherwise a "remember me" session
        // silently downgrades to the standard TTL after its first refresh.
        assertThat(result.refreshToken().expiresAt()).isAfter(OffsetDateTime.now().plusDays(29));

        ArgumentCaptor<RefreshToken> saved = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(saved.capture());
        assertThat(saved.getValue().getRememberMe()).isTrue();
    }

    @Test
    void rotateRejectsUnknownToken() {
        when(tokenGenerator.hash(any())).thenReturn("hash-x");
        when(refreshTokenRepository.findByTokenHash("hash-x")).thenReturn(Optional.empty());

        assertThrows(UnauthorizedException.class, () -> refreshTokenService.rotate("raw-x", "UA", "127.0.0.1"));
    }

    @Test
    void rotateRejectsExpiredToken() {
        RefreshToken existing = new RefreshToken();
        existing.setUser(user);
        existing.setTokenHash("hash-old");
        existing.setExpiresAt(OffsetDateTime.now().minusMinutes(1));

        when(tokenGenerator.hash("raw-old")).thenReturn("hash-old");
        when(refreshTokenRepository.findByTokenHash("hash-old")).thenReturn(Optional.of(existing));

        assertThrows(UnauthorizedException.class, () -> refreshTokenService.rotate("raw-old", "UA", "127.0.0.1"));
    }

    @Test
    void rotateOfAnAlreadyUsedTokenRevokesTheWholeChain() {
        RefreshToken t1 = new RefreshToken();
        t1.setUser(user);
        t1.setTokenHash("hash-1");
        t1.setExpiresAt(OffsetDateTime.now().plusDays(1));
        t1.setRevokedAt(OffsetDateTime.now().minusMinutes(5));
        t1.setReplacedByTokenHash("hash-2");

        RefreshToken t2 = new RefreshToken();
        t2.setUser(user);
        t2.setTokenHash("hash-2");
        t2.setExpiresAt(OffsetDateTime.now().plusDays(1));

        when(tokenGenerator.hash("raw-1")).thenReturn("hash-1");
        when(refreshTokenRepository.findByTokenHash("hash-1")).thenReturn(Optional.of(t1));
        when(refreshTokenRepository.findByTokenHash("hash-2")).thenReturn(Optional.of(t2));

        assertThrows(UnauthorizedException.class, () -> refreshTokenService.rotate("raw-1", "UA", "127.0.0.1"));

        assertThat(t1.getRevokedAt()).isNotNull();
        // whole live chain is killed, not just the replayed token
        assertThat(t2.getRevokedAt()).isNotNull();
    }

    @Test
    void revokeMarksTokenRevoked() {
        RefreshToken token = new RefreshToken();
        token.setTokenHash("hash-1");

        when(tokenGenerator.hash("raw-1")).thenReturn("hash-1");
        when(refreshTokenRepository.findByTokenHash("hash-1")).thenReturn(Optional.of(token));

        refreshTokenService.revoke("raw-1");

        assertThat(token.getRevokedAt()).isNotNull();
    }

    @Test
    void revokeOfUnknownTokenIsANoOp() {
        when(tokenGenerator.hash("raw-x")).thenReturn("hash-x");
        when(refreshTokenRepository.findByTokenHash("hash-x")).thenReturn(Optional.empty());

        refreshTokenService.revoke("raw-x");
        // no exception - logout with a stale/already-cleared cookie should not fail
    }
}
