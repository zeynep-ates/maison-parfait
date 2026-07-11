package com.zeynepates.maisonparfait.backend.modules.product.image;

public record ProductImageResponse(
        Long id,
        String url,
        String altText,
        Integer sortOrder,
        Boolean isPrimary
) {
    public static ProductImageResponse from(ProductImage img) {
        return new ProductImageResponse(
                img.getId(),
                img.getUrl(),
                img.getAltText(),
                img.getSortOrder(),
                img.getIsPrimary()
        );
    }
}
