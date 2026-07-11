package com.zeynepates.maisonparfait.backend.identity.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record UserResponse(

        @Schema(description = "Numeric user id")
        Long id,

        @Schema(description = "Current, confirmed email address")
        String email,

        String fullName,

        @Schema(description = "CUSTOMER or ADMIN")
        String role,

        @Schema(description = "Whether the current email address has been verified")
        boolean emailVerified,

        @Schema(description = "New email awaiting confirmation, if an email change is in progress", nullable = true)
        String pendingEmail
) {
}
