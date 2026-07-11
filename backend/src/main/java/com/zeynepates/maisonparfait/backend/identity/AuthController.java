package com.zeynepates.maisonparfait.backend.identity;

import com.zeynepates.maisonparfait.backend.common.exception.TooManyRequestsException;
import com.zeynepates.maisonparfait.backend.common.exception.UnauthorizedException;
import com.zeynepates.maisonparfait.backend.identity.dto.ForgotPasswordRequest;
import com.zeynepates.maisonparfait.backend.identity.dto.LoginRequest;
import com.zeynepates.maisonparfait.backend.identity.dto.RegisterRequest;
import com.zeynepates.maisonparfait.backend.identity.dto.ResendVerificationRequest;
import com.zeynepates.maisonparfait.backend.identity.dto.ResetPasswordRequest;
import com.zeynepates.maisonparfait.backend.identity.dto.TokenResponse;
import com.zeynepates.maisonparfait.backend.identity.dto.UserResponse;
import com.zeynepates.maisonparfait.backend.identity.dto.VerifyEmailRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.OffsetDateTime;

/**
 * Registration, email verification, login, refresh, and single-device
 * logout. Session-management endpoints (list/revoke sessions, logout
 * everywhere) land in a later milestone (see docs/identity-module-design.md).
 */
@Tag(name = "Auth", description = "Registration, email verification, login, and token refresh")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private static final String REFRESH_TOKEN_COOKIE = "refresh_token";
    private static final String REFRESH_TOKEN_COOKIE_PATH = "/api/auth";
    private static final Duration RATE_LIMIT_WINDOW = Duration.ofMinutes(15);
    private static final int LOGIN_MAX_ATTEMPTS = 5;
    private static final int EMAIL_ACTION_MAX_ATTEMPTS = 3;

    private final AuthService authService;
    private final EmailVerificationService emailVerificationService;
    private final PasswordResetService passwordResetService;
    private final RateLimiter rateLimiter;

    @Operation(summary = "Register a new account")
    @ApiResponse(responseCode = "201", description = "Account created; a verification email is sent asynchronously")
    @ApiResponse(responseCode = "400", description = "Validation failure")
    @ApiResponse(responseCode = "409", description = "Email already registered")
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @Operation(summary = "Confirm an email verification token")
    @ApiResponse(responseCode = "204", description = "Email verified")
    @ApiResponse(responseCode = "400", description = "Invalid, expired, or already-used token")
    @PostMapping("/verify-email")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        emailVerificationService.verify(request.token());
    }

    @Operation(summary = "Resend the email verification link",
            description = "Always returns 202, whether or not the address is registered or already verified.")
    @ApiResponse(responseCode = "202", description = "Accepted")
    @ApiResponse(responseCode = "429", description = "Too many requests for this address")
    @PostMapping("/resend-verification")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void resendVerification(@Valid @RequestBody ResendVerificationRequest request, HttpServletRequest httpRequest) {
        checkRateLimit("resend-verification", httpRequest, request.email(), EMAIL_ACTION_MAX_ATTEMPTS);
        emailVerificationService.resendVerification(request.email());
    }

    @Operation(summary = "Request a password reset email",
            description = "Always returns 202, whether or not the address is registered.")
    @ApiResponse(responseCode = "202", description = "Accepted")
    @ApiResponse(responseCode = "429", description = "Too many requests for this address")
    @PostMapping("/forgot-password")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void forgotPassword(@Valid @RequestBody ForgotPasswordRequest request, HttpServletRequest httpRequest) {
        checkRateLimit("forgot-password", httpRequest, request.email(), EMAIL_ACTION_MAX_ATTEMPTS);
        passwordResetService.forgotPassword(request.email());
    }

    @Operation(summary = "Reset password using a forgot-password token",
            description = "Revokes every existing session for the account.")
    @ApiResponse(responseCode = "204", description = "Password changed")
    @ApiResponse(responseCode = "400", description = "Invalid, expired, or already-used token")
    @PostMapping("/reset-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request.token(), request.newPassword());
    }

    @Operation(summary = "Log in with email and password",
            description = "Returns an access token in the body and sets the refresh token as an httpOnly cookie.")
    @ApiResponse(responseCode = "200", description = "Authenticated")
    @ApiResponse(responseCode = "401", description = "Invalid email or password")
    @ApiResponse(responseCode = "403", description = "Account disabled or locked")
    @ApiResponse(responseCode = "429", description = "Too many attempts for this address")
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        checkRateLimit("login", httpRequest, request.email(), LOGIN_MAX_ATTEMPTS);
        IssuedTokens issued = authService.login(request, httpRequest.getHeader(HttpHeaders.USER_AGENT), httpRequest.getRemoteAddr());
        return withRefreshCookie(issued);
    }

    @Operation(summary = "Rotate the refresh token and issue a new access token",
            description = "Reads the refresh token from its httpOnly cookie. Replaying an already-used token revokes the whole session.")
    @ApiResponse(responseCode = "200", description = "Rotated")
    @ApiResponse(responseCode = "401", description = "Missing, invalid, or expired refresh token")
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(
            @CookieValue(value = REFRESH_TOKEN_COOKIE, required = false) String refreshToken,
            HttpServletRequest httpRequest) {
        if (refreshToken == null) {
            throw new UnauthorizedException("Missing refresh token");
        }
        IssuedTokens issued = authService.refresh(refreshToken, httpRequest.getHeader(HttpHeaders.USER_AGENT), httpRequest.getRemoteAddr());
        return withRefreshCookie(issued);
    }

    @Operation(summary = "Log out this device",
            description = "Revokes only the session tied to the presented refresh token cookie.")
    @ApiResponse(responseCode = "204", description = "Logged out")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@CookieValue(value = REFRESH_TOKEN_COOKIE, required = false) String refreshToken) {
        if (refreshToken != null) {
            authService.logout(refreshToken);
        }
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, clearRefreshCookie().toString())
                .build();
    }

    /**
     * Keyed by IP+email rather than email alone: harder for an attacker to
     * weaponize as a griefing vector against a specific victim's address
     * from an unrelated source (see docs/identity-module-design.md #10).
     */
    private void checkRateLimit(String action, HttpServletRequest httpRequest, String email, int maxAttempts) {
        String key = action + ":" + httpRequest.getRemoteAddr() + ":" + email;
        if (!rateLimiter.tryConsume(key, maxAttempts, RATE_LIMIT_WINDOW)) {
            throw new TooManyRequestsException("Too many attempts - try again later");
        }
    }

    private ResponseEntity<TokenResponse> withRefreshCookie(IssuedTokens issued) {
        long maxAgeSeconds = Duration.between(OffsetDateTime.now(), issued.refreshTokenExpiresAt()).getSeconds();
        ResponseCookie cookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE, issued.rawRefreshToken())
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .path(REFRESH_TOKEN_COOKIE_PATH)
                .maxAge(Math.max(maxAgeSeconds, 0))
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(issued.response());
    }

    private ResponseCookie clearRefreshCookie() {
        return ResponseCookie.from(REFRESH_TOKEN_COOKIE, "")
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .path(REFRESH_TOKEN_COOKIE_PATH)
                .maxAge(0)
                .build();
    }
}
