package com.zeynepates.maisonparfait.backend.modules.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateProductRequest(
        @NotBlank String name,
        @NotNull BigDecimal price,
        @NotBlank String currency
) {
}
