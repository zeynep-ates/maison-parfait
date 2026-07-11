package com.zeynepates.maisonparfait.backend.modules.product.tag;

import com.zeynepates.maisonparfait.backend.modules.tag.Tag;

public record TagResponse(Long id, String name) {
    public static TagResponse from(Tag t) {
        return new TagResponse(t.getId(), t.getName());
    }
}