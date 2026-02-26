package com.zeynepates.maisonparfait.backend.modules.product;

import java.math.BigDecimal;

public record ProductResponse(
        Long id,
        String sku,
        String name,
        String description,
        Long priceCents,
        String currency,
        Integer stock,
        Boolean isActive,
        Boolean isPerishable,
        Integer shelfLifeDays,
        Integer weightGrams
) {
}
