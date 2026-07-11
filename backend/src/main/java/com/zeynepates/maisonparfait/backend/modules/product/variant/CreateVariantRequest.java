package com.zeynepates.maisonparfait.backend.modules.product.variant;

public record CreateVariantRequest(
        String sku,
        String name,
        Long priceCents,
        String currency,
        Integer stock,
        Boolean isActive,
        String attributes
) {}
