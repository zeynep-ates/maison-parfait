package com.zeynepates.maisonparfait.backend.modules.product.variant;

public record ProductVariantResponse(
        Long id,
        String sku,
        String name,
        Long priceCents,
        String currency,
        Integer stock,
        Boolean isActive,
        String attributes
) {
    public static ProductVariantResponse from(ProductVariant v) {
        return new ProductVariantResponse(
                v.getId(),
                v.getSku(),
                v.getName(),
                v.getPriceCents(),
                v.getCurrency(),
                v.getStock(),
                v.getIsActive(),
                v.getAttributes()
        );
    }
}