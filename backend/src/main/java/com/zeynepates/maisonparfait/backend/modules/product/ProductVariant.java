package com.zeynepates.maisonparfait.backend.modules.product;

import com.zeynepates.maisonparfait.backend.modules.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "product_variants")
public class ProductVariant extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "sku", length = 64, nullable = false, unique = true)
    private String sku;

    @Column(name = "name", length = 150, nullable = false)
    private String name;

    @Column(name = "price_cents", nullable = false)
    private Long priceCents;

    @Column(name = "currency", length = 3, nullable = false)
    private String currency = "TRY";

    @Column(name = "stock", nullable = false)
    private Integer stock = 0;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "attributes", columnDefinition = "jsonb", nullable = false)
    private String attributes = "{}";

}
