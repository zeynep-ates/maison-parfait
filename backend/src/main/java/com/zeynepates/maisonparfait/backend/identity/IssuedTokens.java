package com.zeynepates.maisonparfait.backend.identity;

import com.zeynepates.maisonparfait.backend.identity.dto.TokenResponse;

import java.time.OffsetDateTime;

/**
 * Internal handoff between AuthService and AuthController - not a DTO, and
 * deliberately never serialized. The raw refresh token belongs only in a
 * Set-Cookie header, never in a JSON body (see docs/identity-module-design.md).
 * refreshTokenExpiresAt lets the controller set an accurate cookie Max-Age
 * without re-reading TTL configuration itself.
 */
public record IssuedTokens(TokenResponse response, String rawRefreshToken, OffsetDateTime refreshTokenExpiresAt) {
}
