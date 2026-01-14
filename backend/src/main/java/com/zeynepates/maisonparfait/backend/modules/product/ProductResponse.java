package com.zeynepates.maisonparfait.backend.modules.product;

import java.math.BigDecimal;

public record ProductResponse(
        Long id,
        String name,
        BigDecimal price,
        String currency
) {
}
