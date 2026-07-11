package com.zeynepates.maisonparfait.backend.modules.product.category;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductCategoryRepository extends JpaRepository<ProductCategory, ProductCategoryId> {

    boolean existsByProductIdAndCategoryId(Long productId, Long categoryId);

    Optional<ProductCategory> findByProductIdAndCategoryId(Long productId, Long categoryId);

    List<ProductCategory> findAllByProductIdOrderByIdAsc(Long productId);
}
