package com.zeynepates.maisonparfait.backend.modules.product;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class ProductTagId implements Serializable{

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "tag_id")
    private Long tagId;
}
