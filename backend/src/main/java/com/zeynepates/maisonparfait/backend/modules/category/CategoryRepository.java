package com.zeynepates.maisonparfait.backend.modules.category;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    boolean existsBySlug(String slug);
    Optional<Category> findBySlug(String slug);
    List<Category> findAllByKindAndIsActiveTrueOrderBySortOrderAscIdAsc(CategoryKind kind);
}
