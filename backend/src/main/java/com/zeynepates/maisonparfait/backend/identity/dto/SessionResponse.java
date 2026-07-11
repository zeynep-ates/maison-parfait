package com.zeynepates.maisonparfait.backend.identity.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

public record SessionResponse(

        Long id,

        @Schema(description = "Browser/device that created this session, if known", nullable = true)
        String userAgent,

        @Schema(nullable = true)
        String ipAddress,

        OffsetDateTime createdAt,

        @Schema(nullable = true)
        OffsetDateTime lastUsedAt,

        @Schema(description = "Whether this is the session the current request was made with")
        boolean current
) {
}
