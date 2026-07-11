package com.zeynepates.maisonparfait.backend.modules.product.tag;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProductTagId other)) return false;
        return productId != null && productId.equals(other.productId)
                && tagId != null && tagId.equals(other.tagId);
    }

    @Override
    public int hashCode() {
        int result = (productId != null ? productId.hashCode() : 0);
        result = 31 * result + (tagId != null ? tagId.hashCode() : 0);
        return result;
    }
}
