package com.zeynepates.maisonparfait.backend.identity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ForgotPasswordRequest(

        @Schema(example = "jane@example.com")
        @NotBlank(message = "Email is required")
        @Email(message = "Email must be a valid address")
        String email
) {
}
