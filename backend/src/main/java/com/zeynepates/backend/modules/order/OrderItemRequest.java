package com.zeynepates.backend.modules.order;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record OrderItemRequest(

        @NotNull(message = "Product id is required")
        Long productId,

        //items aynı productId’den birden fazla gelirse merge etmek mantıklı
        // olabilir (faz-1’de şart değil ama kolay).

        @Min(value = 1, message = "Quantity must be at least 1")
        int quantity
) {
}
