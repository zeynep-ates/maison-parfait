package com.zeynepates.maisonparfait.backend.identity;

import com.zeynepates.maisonparfait.backend.modules.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailVerificationServiceTest {

    @Mock
    UserRepository userRepository;

    @Mock
    VerificationTokenRepository verificationTokenRepository;

    @Mock
    TokenGenerator tokenGenerator;

    @Mock
    EmailSender emailSender;

    @InjectMocks
    EmailVerificationService emailVerificationService;

    User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("jane@example.com");
    }

    @Test
    void sendVerificationEmailPersistsHashedTokenAndSendsEmail() {
        when(tokenGenerator.generateRawToken()).thenReturn("raw-token");
        when(tokenGenerator.hash("raw-token")).thenReturn("hashed-token");

        emailVerificationService.sendVerificationEmail(user);

        ArgumentCaptor<VerificationToken> saved = ArgumentCaptor.forClass(VerificationToken.class);
        verify(verificationTokenRepository).save(saved.capture());
        assertThat(saved.getValue().getTokenHash()).isEqualTo("hashed-token");
        assertThat(saved.getValue().getType()).isEqualTo(VerificationTokenType.EMAIL_VERIFY);
        assertThat(saved.getValue().getUser()).isEqualTo(user);
        assertThat(saved.getValue().getExpiresAt()).isAfter(OffsetDateTime.now());

        verify(emailSender).send(eq("jane@example.com"), any(), any());
    }

    @Test
    void verifyMarksTokenUsedAndSetsEmailVerifiedAt() {
        VerificationToken token = validToken();
        when(tokenGenerator.hash("raw-token")).thenReturn("hashed-token");
        when(verificationTokenRepository.findByTokenHash("hashed-token")).thenReturn(Optional.of(token));

        emailVerificationService.verify("raw-token");

        assertThat(token.getUsedAt()).isNotNull();
        assertThat(user.getEmailVerifiedAt()).isNotNull();
    }

    @Test
    void verifyRejectsUnknownToken() {
        when(tokenGenerator.hash(any())).thenReturn("hashed-token");
        when(verificationTokenRepository.findByTokenHash("hashed-token")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> emailVerificationService.verify("raw-token"));
    }

    @Test
    void verifyRejectsExpiredToken() {
        VerificationToken token = validToken();
        token.setExpiresAt(OffsetDateTime.now().minusMinutes(1));
        when(tokenGenerator.hash(any())).thenReturn("hashed-token");
        when(verificationTokenRepository.findByTokenHash("hashed-token")).thenReturn(Optional.of(token));

        assertThrows(IllegalArgumentException.class, () -> emailVerificationService.verify("raw-token"));
        assertThat(user.getEmailVerifiedAt()).isNull();
    }

    @Test
    void verifyRejectsAlreadyUsedToken() {
        VerificationToken token = validToken();
        token.setUsedAt(OffsetDateTime.now().minusMinutes(1));
        when(tokenGenerator.hash(any())).thenReturn("hashed-token");
        when(verificationTokenRepository.findByTokenHash("hashed-token")).thenReturn(Optional.of(token));

        assertThrows(IllegalArgumentException.class, () -> emailVerificationService.verify("raw-token"));
    }

    @Test
    void verifyRejectsWrongTokenType() {
        VerificationToken token = validToken();
        token.setType(VerificationTokenType.PASSWORD_RESET);
        when(tokenGenerator.hash(any())).thenReturn("hashed-token");
        when(verificationTokenRepository.findByTokenHash("hashed-token")).thenReturn(Optional.of(token));

        assertThrows(IllegalArgumentException.class, () -> emailVerificationService.verify("raw-token"));
    }

    @Test
    void resendVerificationSendsWhenUserExistsAndUnverified() {
        when(userRepository.findByEmailAndDeletedAtIsNull("jane@example.com")).thenReturn(Optional.of(user));
        when(tokenGenerator.generateRawToken()).thenReturn("raw-token");
        when(tokenGenerator.hash("raw-token")).thenReturn("hashed-token");

        emailVerificationService.resendVerification("jane@example.com");

        verify(verificationTokenRepository, times(1)).save(any());
        verify(emailSender, times(1)).send(eq("jane@example.com"), any(), any());
    }

    @Test
    void resendVerificationIsNoOpWhenUserDoesNotExist() {
        when(userRepository.findByEmailAndDeletedAtIsNull("ghost@example.com")).thenReturn(Optional.empty());

        emailVerificationService.resendVerification("ghost@example.com");

        verify(verificationTokenRepository, never()).save(any());
        verify(emailSender, never()).send(any(), any(), any());
    }

    @Test
    void resendVerificationIsNoOpWhenAlreadyVerified() {
        user.setEmailVerifiedAt(OffsetDateTime.now());
        when(userRepository.findByEmailAndDeletedAtIsNull("jane@example.com")).thenReturn(Optional.of(user));

        emailVerificationService.resendVerification("jane@example.com");

        verify(verificationTokenRepository, never()).save(any());
        verify(emailSender, never()).send(any(), any(), any());
    }

    private VerificationToken validToken() {
        VerificationToken token = new VerificationToken();
        token.setUser(user);
        token.setType(VerificationTokenType.EMAIL_VERIFY);
        token.setTokenHash("hashed-token");
        token.setExpiresAt(OffsetDateTime.now().plusHours(1));
        return token;
    }
}
