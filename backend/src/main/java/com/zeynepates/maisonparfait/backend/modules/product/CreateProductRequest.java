package com.zeynepates.maisonparfait.backend.modules.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record CreateProductRequest(
        @NotBlank @Size(max = 64) String sku,
        @NotBlank @Size(max = 255) String name,
        String description,
        @NotNull @PositiveOrZero Long priceCents,
        @Size(min = 3, max = 3) String currency,
        @NotNull @PositiveOrZero Integer stock,
        Boolean isActive,
        Boolean isPerishable,
        @PositiveOrZero Integer shelfLifeDays,
        @PositiveOrZero Integer weightGrams
) {
}
