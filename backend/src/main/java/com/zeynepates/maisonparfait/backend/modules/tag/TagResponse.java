package com.zeynepates.maisonparfait.backend.modules.tag;

public record TagResponse(
        Long id,
        String type,
        String name,
        String slug,
        Integer sortOrder,
        Boolean isActive
) {
    public static TagResponse from(Tag t) {
        return new TagResponse(
                t.getId(),
                t.getType().name(),
                t.getName(),
                t.getSlug(),
                t.getSortOrder(),
                t.getIsActive()
        );
    }
}
