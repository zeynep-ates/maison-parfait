package com.zeynepates.maisonparfait.backend.modules.admin;

import com.zeynepates.maisonparfait.backend.modules.category.CategoryKind;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AdminCategoryRequest(
        @NotBlank String name,
        @NotBlank String slug,
        Long parentId,
        @NotNull CategoryKind kind,
        Integer sortOrder,
        Boolean isActive
) {}
