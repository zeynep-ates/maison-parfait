package com.zeynepates.maisonparfait.backend.modules.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    boolean existsBySku(String sku);

    Optional<Product> findByIdAndIsActiveTrue(Long id);

    Page<Product> findAllByIsActiveTrue(Pageable pageable);

    Page<Product> findAllByIsActiveFalse(Pageable pageable);

    @Query("""
    select distinct p
    from Product p
    left join fetch p.images
    where p.id = :id
""")
    Optional<Product> findByIdWithImages(@Param("id") Long id);

    @Query("""
    select distinct p
    from Product p
    left join fetch p.images
""")
    List<Product> findAllWithImages();

    @Query("""
select distinct p
from Product p
left join fetch p.images i
left join fetch p.productTags pt
left join fetch pt.tag t
where p.id = :id
""")
    Optional<Product> findByIdWithImagesAndTags(Long id);
}
