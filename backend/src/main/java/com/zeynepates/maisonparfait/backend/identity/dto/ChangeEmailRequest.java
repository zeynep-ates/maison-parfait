package com.zeynepates.maisonparfait.backend.identity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ChangeEmailRequest(

        @Schema(description = "New email - not active until its confirmation link is visited", example = "jane.new@example.com")
        @NotBlank(message = "Email is required")
        @Email(message = "Email must be a valid address")
        String newEmail
) {
}
