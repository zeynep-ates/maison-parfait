package com.zeynepates.maisonparfait.backend.modules.tag;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TagRepository extends JpaRepository<Tag, Long> {
    boolean existsBySlug(String slug);
    List<Tag> findAllByTypeAndIsActiveTrueOrderBySortOrderAscIdAsc(TagType type);
}