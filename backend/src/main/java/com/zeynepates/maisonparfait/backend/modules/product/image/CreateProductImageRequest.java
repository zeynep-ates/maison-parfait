package com.zeynepates.maisonparfait.backend.modules.product.image;

import jakarta.validation.constraints.NotBlank;

public record CreateProductImageRequest(
        @NotBlank String url,
        String altText,
        Integer sortOrder,
        Boolean isPrimary
) {}