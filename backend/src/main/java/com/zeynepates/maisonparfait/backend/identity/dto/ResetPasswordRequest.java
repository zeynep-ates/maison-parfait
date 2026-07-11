package com.zeynepates.maisonparfait.backend.identity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(

        @Schema(description = "Raw token from the forgot-password email link")
        @NotBlank(message = "Token is required")
        String token,

        @Schema(description = "New plaintext password, hashed with BCrypt before storage")
        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 72, message = "Password must be between 8 and 72 characters")
        String newPassword
) {
}
