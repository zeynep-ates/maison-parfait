package com.zeynepates.maisonparfait.backend.modules.product.tag;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductTagRepository extends JpaRepository<ProductTag, ProductTagId> {

    boolean existsByProductIdAndTagId(Long productId, Long tagId);

    Optional<ProductTag> findByProductIdAndTagId(Long productId, Long tagId);

    List<ProductTag> findAllByProductIdOrderByIdAsc(Long productId);
}
