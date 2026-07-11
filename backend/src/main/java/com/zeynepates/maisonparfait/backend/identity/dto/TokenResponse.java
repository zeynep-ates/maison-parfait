package com.zeynepates.maisonparfait.backend.identity.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record TokenResponse(

        @Schema(description = "Short-lived JWT - hold in memory only, send as \"Authorization: Bearer <token>\"")
        String accessToken,

        @Schema(example = "Bearer")
        String tokenType,

        @Schema(description = "Access token lifetime in seconds")
        long expiresInSeconds,

        UserResponse user
) {
}
