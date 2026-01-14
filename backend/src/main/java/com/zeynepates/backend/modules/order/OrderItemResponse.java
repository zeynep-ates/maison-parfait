package com.zeynepates.backend.modules.order;

import java.math.BigDecimal;

public record OrderItemResponse(

        Long productId,
        String productName,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal totalPrice
) {
}
