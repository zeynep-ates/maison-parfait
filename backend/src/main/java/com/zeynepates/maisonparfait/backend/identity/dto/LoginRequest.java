package com.zeynepates.maisonparfait.backend.identity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(

        @Schema(example = "jane@example.com")
        @NotBlank(message = "Email is required")
        String email,

        @Schema(description = "Plaintext password - never logged")
        @NotBlank(message = "Password is required")
        String password,

        @Schema(description = "If true, the refresh token gets a longer TTL instead of a separate mechanism")
        boolean rememberMe
) {
}
