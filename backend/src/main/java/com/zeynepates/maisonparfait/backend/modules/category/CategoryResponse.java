package com.zeynepates.maisonparfait.backend.modules.category;

public record CategoryResponse(
        Long id,
        String name,
        String slug,
        Long parentId,
        String kind,
        Integer sortOrder,
        Boolean isActive
) {
    public static CategoryResponse from(Category c) {
        return new CategoryResponse(
                c.getId(),
                c.getName(),
                c.getSlug(),
                c.getParent() != null ? c.getParent().getId() : null,
                c.getKind().name(),
                c.getSortOrder(),
                c.getIsActive()
        );
    }
}
