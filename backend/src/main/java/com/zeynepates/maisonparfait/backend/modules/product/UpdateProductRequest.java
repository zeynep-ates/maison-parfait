package com.zeynepates.maisonparfait.backend.modules.product;

import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record UpdateProductRequest(
        @Size(max = 64) String sku,
        @Size(max = 255) String name,
        String description,
        @PositiveOrZero Long priceCents,
        @Size(min = 3, max = 3) String currency,
        @PositiveOrZero Integer stock,
        Boolean isActive,
        Boolean isPerishable,
        @PositiveOrZero Integer shelfLifeDays,
        @PositiveOrZero Integer weightGrams
) {
}
