package com.zeynepates.maisonparfait.backend.identity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record VerifyEmailRequest(

        @Schema(description = "Raw verification token from the emailed link")
        @NotBlank(message = "Token is required")
        String token
) {
}
