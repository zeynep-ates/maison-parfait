package com.zeynepates.maisonparfait.backend.identity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock
    UserRepository userRepository;

    @Mock
    VerificationTokenRepository verificationTokenRepository;

    @Mock
    TokenGenerator tokenGenerator;

    @Mock
    EmailSender emailSender;

    @Mock
    PasswordEncoder passwordEncoder;

    @Mock
    RefreshTokenService refreshTokenService;

    @InjectMocks
    PasswordResetService passwordResetService;

    User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("jane@example.com");
        user.setPasswordHash("old-hash");
    }

    @Test
    void forgotPasswordSendsTokenWhenUserExists() {
        when(userRepository.findByEmailAndDeletedAtIsNull("jane@example.com")).thenReturn(Optional.of(user));
        when(tokenGenerator.generateRawToken()).thenReturn("raw-token");
        when(tokenGenerator.hash("raw-token")).thenReturn("hashed-token");

        passwordResetService.forgotPassword("jane@example.com");

        ArgumentCaptor<VerificationToken> saved = ArgumentCaptor.forClass(VerificationToken.class);
        verify(verificationTokenRepository).save(saved.capture());
        assertThat(saved.getValue().getType()).isEqualTo(VerificationTokenType.PASSWORD_RESET);
        assertThat(saved.getValue().getExpiresAt()).isBefore(OffsetDateTime.now().plusMinutes(31));
        verify(emailSender).send(eq("jane@example.com"), any(), any());
    }

    @Test
    void forgotPasswordIsNoOpWhenUserDoesNotExist() {
        when(userRepository.findByEmailAndDeletedAtIsNull("ghost@example.com")).thenReturn(Optional.empty());

        passwordResetService.forgotPassword("ghost@example.com");

        verify(verificationTokenRepository, never()).save(any());
        verify(emailSender, never()).send(any(), any(), any());
    }

    @Test
    void resetPasswordUpdatesHashAndRevokesAllSessions() {
        VerificationToken token = validToken();
        when(tokenGenerator.hash("raw-token")).thenReturn("hashed-token");
        when(verificationTokenRepository.findByTokenHash("hashed-token")).thenReturn(Optional.of(token));
        when(passwordEncoder.encode("newPassword123")).thenReturn("new-hash");

        passwordResetService.resetPassword("raw-token", "newPassword123");

        assertThat(token.getUsedAt()).isNotNull();
        assertThat(user.getPasswordHash()).isEqualTo("new-hash");
        verify(refreshTokenService).revokeAllForUser(1L);
    }

    @Test
    void resetPasswordRejectsUnknownToken() {
        when(tokenGenerator.hash(any())).thenReturn("hashed-token");
        when(verificationTokenRepository.findByTokenHash("hashed-token")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> passwordResetService.resetPassword("raw-token", "newPassword123"));
        verify(refreshTokenService, never()).revokeAllForUser(any());
    }

    @Test
    void resetPasswordRejectsExpiredToken() {
        VerificationToken token = validToken();
        token.setExpiresAt(OffsetDateTime.now().minusMinutes(1));
        when(tokenGenerator.hash(any())).thenReturn("hashed-token");
        when(verificationTokenRepository.findByTokenHash("hashed-token")).thenReturn(Optional.of(token));

        assertThrows(IllegalArgumentException.class, () -> passwordResetService.resetPassword("raw-token", "newPassword123"));
    }

    @Test
    void resetPasswordRejectsAlreadyUsedToken() {
        VerificationToken token = validToken();
        token.setUsedAt(OffsetDateTime.now().minusMinutes(1));
        when(tokenGenerator.hash(any())).thenReturn("hashed-token");
        when(verificationTokenRepository.findByTokenHash("hashed-token")).thenReturn(Optional.of(token));

        assertThrows(IllegalArgumentException.class, () -> passwordResetService.resetPassword("raw-token", "newPassword123"));
    }

    @Test
    void resetPasswordRejectsWrongTokenType() {
        VerificationToken token = validToken();
        token.setType(VerificationTokenType.EMAIL_VERIFY);
        when(tokenGenerator.hash(any())).thenReturn("hashed-token");
        when(verificationTokenRepository.findByTokenHash("hashed-token")).thenReturn(Optional.of(token));

        assertThrows(IllegalArgumentException.class, () -> passwordResetService.resetPassword("raw-token", "newPassword123"));
    }

    private VerificationToken validToken() {
        VerificationToken token = new VerificationToken();
        token.setUser(user);
        token.setType(VerificationTokenType.PASSWORD_RESET);
        token.setTokenHash("hashed-token");
        token.setExpiresAt(OffsetDateTime.now().plusMinutes(30));
        return token;
    }
}
