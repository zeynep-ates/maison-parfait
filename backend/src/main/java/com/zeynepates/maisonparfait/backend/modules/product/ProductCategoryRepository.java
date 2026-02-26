package com.zeynepates.maisonparfait.backend.modules.product;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductCategoryRepository
        extends JpaRepository<ProductCategory, ProductCategoryId> {

    List<ProductCategory> findByProductId(Long productId);

    List<ProductCategory> findByCategoryId(Long categoryId);
}
