package com.zeynepates.maisonparfait.backend.identity;

import com.zeynepates.maisonparfait.backend.common.exception.ConflictException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private static final int VERIFICATION_TOKEN_TTL_HOURS = 24;
    private static final String INVALID_TOKEN_MESSAGE = "Invalid or expired verification token";

    private final UserRepository userRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final TokenGenerator tokenGenerator;
    private final EmailSender emailSender;

    @Transactional
    public void sendVerificationEmail(User user) {
        String rawToken = issueToken(user, VerificationTokenType.EMAIL_VERIFY);
        String body = "Verify your email by opening this link: https://maisonparfait.example/verify-email?token=" + rawToken;
        emailSender.send(user.getEmail(), "Verify your email", body);
    }

    /**
     * Doesn't change User.email yet - only sets pendingEmail and sends a
     * confirmation link to the *new* address, which is the step that
     * actually proves the caller controls it. A heads-up notice also goes
     * to the current (old) address as cheap defense-in-depth: if an
     * attacker with a stolen session tries to quietly take over the
     * account this way, the legitimate owner has a chance to notice and
     * react (e.g. reset their password) before the change is confirmed.
     */
    @Transactional
    public void initiateEmailChange(User user, String newEmail) {
        user.setPendingEmail(newEmail);
        String rawToken = issueToken(user, VerificationTokenType.EMAIL_CHANGE);

        String confirmBody = "Confirm your new email by opening this link: https://maisonparfait.example/verify-email?token=" + rawToken;
        emailSender.send(newEmail, "Confirm your new email", confirmBody);

        String noticeBody = "Your account email is being changed to " + newEmail
                + ". If this wasn't you, reset your password immediately.";
        emailSender.send(user.getEmail(), "Your email is being changed", noticeBody);
    }

    /**
     * Dispatches on the token's own type - EMAIL_VERIFY confirms the
     * current email, EMAIL_CHANGE promotes pendingEmail into email.
     * PASSWORD_RESET tokens are deliberately rejected here: resetting a
     * password needs an extra input (the new password) this endpoint
     * doesn't take, so that flow has its own separate entry point in
     * PasswordResetService instead of sharing this one.
     */
    @Transactional
    public void verify(String rawToken) {
        VerificationToken token = verificationTokenRepository.findByTokenHash(tokenGenerator.hash(rawToken))
                .orElseThrow(() -> new IllegalArgumentException(INVALID_TOKEN_MESSAGE));

        if (token.getUsedAt() != null || token.getExpiresAt().isBefore(OffsetDateTime.now())) {
            throw new IllegalArgumentException(INVALID_TOKEN_MESSAGE);
        }

        User user = token.getUser();
        switch (token.getType()) {
            case EMAIL_VERIFY -> user.setEmailVerifiedAt(OffsetDateTime.now());
            case EMAIL_CHANGE -> {
                // Re-check at confirmation time, not just at request time:
                // someone else could have registered this exact address in
                // the window between the change being requested and now.
                // Without this, the flush below would hit the partial
                // unique index and surface as an opaque 500 instead of a
                // clear error.
                if (userRepository.existsByEmailAndDeletedAtIsNull(user.getPendingEmail())) {
                    throw new ConflictException("Email already registered");
                }
                user.setEmail(user.getPendingEmail());
                user.setPendingEmail(null);
                user.setEmailVerifiedAt(OffsetDateTime.now());
            }
            default -> throw new IllegalArgumentException(INVALID_TOKEN_MESSAGE);
        }

        token.setUsedAt(OffsetDateTime.now());
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

    private String issueToken(User user, VerificationTokenType type) {
        String rawToken = tokenGenerator.generateRawToken();

        VerificationToken token = new VerificationToken();
        token.setUser(user);
        token.setType(type);
        token.setTokenHash(tokenGenerator.hash(rawToken));
        token.setExpiresAt(OffsetDateTime.now().plusHours(VERIFICATION_TOKEN_TTL_HOURS));
        verificationTokenRepository.save(token);

        return rawToken;
    }
}
