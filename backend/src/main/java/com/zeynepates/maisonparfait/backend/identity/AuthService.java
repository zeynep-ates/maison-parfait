package com.zeynepates.maisonparfait.backend.identity;

import com.zeynepates.maisonparfait.backend.common.exception.ConflictException;
import com.zeynepates.maisonparfait.backend.common.exception.ForbiddenException;
import com.zeynepates.maisonparfait.backend.common.exception.NotFoundException;
import com.zeynepates.maisonparfait.backend.common.exception.UnauthorizedException;
import com.zeynepates.maisonparfait.backend.identity.dto.ChangePasswordRequest;
import com.zeynepates.maisonparfait.backend.identity.dto.LoginRequest;
import com.zeynepates.maisonparfait.backend.identity.dto.RegisterRequest;
import com.zeynepates.maisonparfait.backend.identity.dto.TokenResponse;
import com.zeynepates.maisonparfait.backend.identity.dto.UserResponse;
import com.zeynepates.maisonparfait.backend.identity.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String INVALID_CREDENTIALS_MESSAGE = "Invalid email or password";

    /**
     * BCrypt hash of an arbitrary, never-used value. Compared against on
     * the "no such user" path purely so that branch costs the same as a
     * real password check - otherwise a nonexistent-email request returns
     * measurably faster than a wrong-password one, leaking whether an
     * email is registered through response timing alone.
     */
    private static final String DUMMY_HASH = "$2a$10$7EqJtq98hPqEX7fNZaFWoOhi5L4pTjE59.tpVMTIQVj9zUkI50V0y";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailVerificationService emailVerificationService;
    private final UserMapper userMapper;
    private final TokenService tokenService;
    private final RefreshTokenService refreshTokenService;
    private final SessionService sessionService;

    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByEmailAndDeletedAtIsNull(request.email())) {
            throw new ConflictException("Email already registered");
        }

        User user = new User();
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setFullName(request.fullName());
        user.setRole(UserRole.CUSTOMER);
        user.setEnabled(true);

        User saved = userRepository.save(user);
        emailVerificationService.sendVerificationEmail(saved);

        return userMapper.toResponse(saved);
    }

    @Transactional
    public IssuedTokens login(LoginRequest request, String userAgent, String ipAddress) {
        User user = userRepository.findByEmailAndDeletedAtIsNull(request.email()).orElse(null);

        String hashToCheck = (user != null && user.getPasswordHash() != null)
                ? user.getPasswordHash()
                : DUMMY_HASH;
        boolean passwordMatches = passwordEncoder.matches(request.password(), hashToCheck);

        if (user == null || !passwordMatches) {
            throw new UnauthorizedException(INVALID_CREDENTIALS_MESSAGE);
        }

        // Account-state checks only ever happen after a correct password is
        // proven - otherwise they'd become an enumeration side-channel of
        // their own (probing whether an account is disabled/locked without
        // knowing its password). See docs/identity-module-design.md #3.
        // A soft-deleted account never reaches this point at all: it's
        // filtered out at the query level by findByEmailAndDeletedAtIsNull
        // above, which folds "deleted" and "never existed" into the exact
        // same user == null branch - a stronger guarantee than an app-level
        // check, since it can't be bypassed by a future caller that forgets it.
        if (Boolean.FALSE.equals(user.getEnabled())) {
            throw new ForbiddenException("Account disabled");
        }
        if (user.getLockedAt() != null) {
            throw new ForbiddenException("Account locked");
        }

        RefreshTokenService.IssuedRefreshToken refreshToken =
                refreshTokenService.issue(user, userAgent, ipAddress, request.rememberMe());
        return new IssuedTokens(buildTokenResponse(user), refreshToken.rawToken(), refreshToken.expiresAt());
    }

    @Transactional
    public IssuedTokens refresh(String rawRefreshToken, String userAgent, String ipAddress) {
        RefreshTokenService.RotationResult rotation = refreshTokenService.rotate(rawRefreshToken, userAgent, ipAddress);
        return new IssuedTokens(
                buildTokenResponse(rotation.user()),
                rotation.refreshToken().rawToken(),
                rotation.refreshToken().expiresAt());
    }

    @Transactional
    public void logout(String rawRefreshToken) {
        refreshTokenService.revoke(rawRefreshToken);
    }

    /**
     * Requires the current password. Revokes every other active session -
     * the calling session (identified by its own refresh token) is left
     * alone, since there's no compromise signal here, just a routine
     * credential change.
     */
    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request, String currentRawRefreshToken) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));

        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException("Current password is incorrect");
        }

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        sessionService.revokeAllExceptCurrent(userId, currentRawRefreshToken);
    }

    /**
     * Doesn't change the email yet - see EmailVerificationService.initiateEmailChange.
     */
    @Transactional
    public void changeEmail(Long userId, String newEmail) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));

        if (userRepository.existsByEmailAndDeletedAtIsNull(newEmail)) {
            throw new ConflictException("Email already registered");
        }

        emailVerificationService.initiateEmailChange(user, newEmail);
    }

    /**
     * The single place every authentication mechanism converges once a User
     * has been established - password login today, a future OAuth provider
     * tomorrow. A future loginWithOAuth(...) should resolve/create its User
     * row and then call this same method, not duplicate token issuance.
     */
    private TokenResponse buildTokenResponse(User user) {
        String accessToken = tokenService.generateAccessToken(user.getId(), user.getRole());
        return new TokenResponse(accessToken, "Bearer", tokenService.getAccessTokenTtlSeconds(), userMapper.toResponse(user));
    }
}
