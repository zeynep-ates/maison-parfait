package com.zeynepates.maisonparfait.backend.modules.admin;

import com.zeynepates.maisonparfait.backend.modules.tag.TagType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AdminTagRequest(
        @NotNull TagType type,
        @NotBlank String name,
        @NotBlank String slug,
        Integer sortOrder,
        Boolean isActive
) {}
