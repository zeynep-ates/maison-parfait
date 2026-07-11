package com.zeynepates.maisonparfait.backend.modules.product;

import com.zeynepates.maisonparfait.backend.modules.product.category.CategoryResponse;
import com.zeynepates.maisonparfait.backend.modules.product.image.ProductImageResponse;
import com.zeynepates.maisonparfait.backend.modules.product.tag.TagResponse;
import com.zeynepates.maisonparfait.backend.modules.product.variant.ProductVariantResponse;

import java.util.List;

public record ProductResponse(
        Long id,
        String sku,
        String name,
        String description,
        Long priceCents,
        String currency,
        Integer stock,
        Boolean isActive,
        Boolean isPerishable,
        Integer shelfLifeDays,
        Integer weightGrams,
        List<ProductImageResponse> images,
        List<TagResponse> tags,
        List<CategoryResponse> categories,
        List<ProductVariantResponse> variants
) {}