package com.zeynepates.maisonparfait.backend.modules.product.category;

import com.zeynepates.maisonparfait.backend.modules.category.Category;

public record CategoryResponse(Long id, String name, String slug) {
    public static CategoryResponse from(Category c) {
        return new CategoryResponse(c.getId(), c.getName(), c.getSlug());
    }
}