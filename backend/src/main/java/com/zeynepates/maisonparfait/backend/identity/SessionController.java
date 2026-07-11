package com.zeynepates.maisonparfait.backend.identity;

import com.zeynepates.maisonparfait.backend.common.security.AuthenticatedUser;
import com.zeynepates.maisonparfait.backend.common.security.CurrentUser;
import com.zeynepates.maisonparfait.backend.identity.dto.SessionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * One session per device/browser (one refresh_tokens row each). The first
 * real consumer of the CurrentUser/@AuthenticatedUser infrastructure built
 * in Phase 0 - populated for real since Phase 1C's JwtAuthenticationFilter.
 */
@Tag(name = "Sessions", description = "Manage active login sessions (devices)")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users/me/sessions")
public class SessionController {

    private static final String REFRESH_TOKEN_COOKIE = "refresh_token";

    private final SessionService sessionService;

    @Operation(summary = "List active sessions for the current user")
    @ApiResponse(responseCode = "200", description = "Active, non-expired sessions")
    @GetMapping
    public List<SessionResponse> list(
            @AuthenticatedUser CurrentUser currentUser,
            @CookieValue(value = REFRESH_TOKEN_COOKIE, required = false) String refreshToken) {
        return sessionService.listActiveSessions(currentUser.id(), refreshToken);
    }

    @Operation(summary = "Revoke one session (log out that device)")
    @ApiResponse(responseCode = "204", description = "Revoked")
    @ApiResponse(responseCode = "404", description = "No such session for this user")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void revokeOne(@AuthenticatedUser CurrentUser currentUser, @PathVariable Long id) {
        sessionService.revokeSession(currentUser.id(), id);
    }

    @Operation(summary = "Revoke all sessions except the current one",
            description = "\"Log out everywhere else\" - the session tied to the request's own refresh token cookie is left active.")
    @ApiResponse(responseCode = "204", description = "Revoked")
    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void revokeAllExceptCurrent(
            @AuthenticatedUser CurrentUser currentUser,
            @CookieValue(value = REFRESH_TOKEN_COOKIE, required = false) String refreshToken) {
        sessionService.revokeAllExceptCurrent(currentUser.id(), refreshToken);
    }
}
