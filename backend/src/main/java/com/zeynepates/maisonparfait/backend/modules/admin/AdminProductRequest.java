package com.zeynepates.maisonparfait.backend.modules.admin;

import jakarta.validation.constraints.*;

public record AdminProductRequest(

        @NotBlank String sku,
        @NotBlank String name,
        String description,

        @NotNull @Positive Long priceCents,

        @NotBlank @Size(min = 3, max = 3)
        String currency,

        @NotNull @Min(0)
        Integer stock,

        @NotNull Boolean isActive,
        @NotNull Boolean isPerishable,

        Integer shelfLifeDays,
        Integer weightGrams

) {}
