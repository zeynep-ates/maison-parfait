package com.zeynepates.maisonparfait.backend.identity;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private static final int PASSWORD_RESET_TTL_MINUTES = 30;
    private static final String INVALID_TOKEN_MESSAGE = "Invalid or expired reset token";

    private final UserRepository userRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final TokenGenerator tokenGenerator;
    private final EmailSender emailSender;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;

    /**
     * Enumeration-safe: does nothing observable if the email doesn't exist,
     * so the controller always responds 202 regardless.
     */
    @Transactional
    public void forgotPassword(String email) {
        userRepository.findByEmailAndDeletedAtIsNull(email).ifPresent(user -> {
            String rawToken = tokenGenerator.generateRawToken();

            VerificationToken token = new VerificationToken();
            token.setUser(user);
            token.setType(VerificationTokenType.PASSWORD_RESET);
            token.setTokenHash(tokenGenerator.hash(rawToken));
            token.setExpiresAt(OffsetDateTime.now().plusMinutes(PASSWORD_RESET_TTL_MINUTES));
            verificationTokenRepository.save(token);

            String body = "Reset your password by opening this link: https://maisonparfait.example/reset-password?token=" + rawToken;
            emailSender.send(user.getEmail(), "Reset your password", body);
        });
    }

    /**
     * On success, every refresh token for the account is revoked - a
     * password reset is frequently a response to suspected compromise, so
     * every existing session should die and force re-login everywhere,
     * including the device the reset itself was performed from.
     */
    @Transactional
    public void resetPassword(String rawToken, String newPassword) {
        VerificationToken token = verificationTokenRepository.findByTokenHash(tokenGenerator.hash(rawToken))
                .orElseThrow(() -> new IllegalArgumentException(INVALID_TOKEN_MESSAGE));

        if (token.getType() != VerificationTokenType.PASSWORD_RESET
                || token.getUsedAt() != null
                || token.getExpiresAt().isBefore(OffsetDateTime.now())) {
            throw new IllegalArgumentException(INVALID_TOKEN_MESSAGE);
        }

        token.setUsedAt(OffsetDateTime.now());

        User user = token.getUser();
        user.setPasswordHash(passwordEncoder.encode(newPassword));

        refreshTokenService.revokeAllForUser(user.getId());
    }
}
