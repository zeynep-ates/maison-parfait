package com.zeynepates.maisonparfait.backend.identity;

import com.zeynepates.maisonparfait.backend.common.security.AuthenticatedUser;
import com.zeynepates.maisonparfait.backend.common.security.CurrentUser;
import com.zeynepates.maisonparfait.backend.identity.dto.ChangeEmailRequest;
import com.zeynepates.maisonparfait.backend.identity.dto.ChangePasswordRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Only change-password and change-email exist so far - profile GET/PATCH
 * and self-service account deletion aren't in scope yet (see
 * docs/identity-module-design.md's endpoint table for the full set this
 * grows into).
 */
@Tag(name = "Users", description = "Current user's account operations")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users/me")
public class UserController {

    private static final String REFRESH_TOKEN_COOKIE = "refresh_token";

    private final AuthService authService;

    @Operation(summary = "Change password",
            description = "Requires the current password. Revokes every other active session; the calling session stays logged in.")
    @ApiResponse(responseCode = "204", description = "Changed")
    @ApiResponse(responseCode = "401", description = "Current password is incorrect")
    @PostMapping("/change-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(
            @AuthenticatedUser CurrentUser currentUser,
            @Valid @RequestBody ChangePasswordRequest request,
            @CookieValue(value = REFRESH_TOKEN_COOKIE, required = false) String refreshToken) {
        authService.changePassword(currentUser.id(), request, refreshToken);
    }

    @Operation(summary = "Request an email change",
            description = "Does not change the email yet - sends a confirmation link to the new address, which must be visited to complete the change.")
    @ApiResponse(responseCode = "202", description = "Accepted")
    @ApiResponse(responseCode = "409", description = "Email already registered")
    @PostMapping("/change-email")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void changeEmail(@AuthenticatedUser CurrentUser currentUser, @Valid @RequestBody ChangeEmailRequest request) {
        authService.changeEmail(currentUser.id(), request.newEmail());
    }
}
