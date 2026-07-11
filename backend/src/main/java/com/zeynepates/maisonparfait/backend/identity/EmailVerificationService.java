package com.zeynepates.maisonparfait.backend.identity;

import com.zeynepates.maisonparfait.backend.modules.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private static final int EMAIL_VERIFY_TTL_HOURS = 24;
    private static final String INVALID_TOKEN_MESSAGE = "Invalid or expired verification token";

    private final UserRepository userRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final TokenGenerator tokenGenerator;
    private final EmailSender emailSender;

    @Transactional
    public void sendVerificationEmail(User user) {
        String rawToken = tokenGenerator.generateRawToken();

        VerificationToken token = new VerificationToken();
        token.setUser(user);
        token.setType(VerificationTokenType.EMAIL_VERIFY);
        token.setTokenHash(tokenGenerator.hash(rawToken));
        token.setExpiresAt(OffsetDateTime.now().plusHours(EMAIL_VERIFY_TTL_HOURS));
        verificationTokenRepository.save(token);

        String body = "Verify your email by opening this link: https://maisonparfait.example/verify-email?token=" + rawToken;
        emailSender.send(user.getEmail(), "Verify your email", body);
    }

    /**
     * Only EMAIL_VERIFY tokens can exist yet - PASSWORD_RESET and
     * EMAIL_CHANGE flows land in a later milestone and will extend this
     * method with their own branches then, not before.
     */
    @Transactional
    public void verify(String rawToken) {
        VerificationToken token = verificationTokenRepository.findByTokenHash(tokenGenerator.hash(rawToken))
                .orElseThrow(() -> new IllegalArgumentException(INVALID_TOKEN_MESSAGE));

        if (token.getType() != VerificationTokenType.EMAIL_VERIFY
                || token.getUsedAt() != null
                || token.getExpiresAt().isBefore(OffsetDateTime.now())) {
            throw new IllegalArgumentException(INVALID_TOKEN_MESSAGE);
        }

        token.setUsedAt(OffsetDateTime.now());
        token.getUser().setEmailVerifiedAt(OffsetDateTime.now());
    }

    /**
     * Enumeration-safe by construction: does nothing observable whether the
     * email doesn't exist or is already verified, so the controller can
     * always respond 202 regardless.
     */
    @Transactional
    public void resendVerification(String email) {
        userRepository.findByEmailAndDeletedAtIsNull(email)
                .filter(user -> user.getEmailVerifiedAt() == null)
                .ifPresent(this::sendVerificationEmail);
    }
}
