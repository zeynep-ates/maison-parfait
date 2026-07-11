package com.zeynepates.maisonparfait.backend.modules.product.image;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
    List<ProductImage> findAllByProductIdOrderBySortOrderAscIdAsc(Long productId);
}
