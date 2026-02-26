package com.zeynepates.maisonparfait.backend.modules.order;

public record OrderItemResponse(

        Long productId,
        String productName,
        int quantity,
        Long unitPriceCents,
        Long totalPriceCents
) {
}
