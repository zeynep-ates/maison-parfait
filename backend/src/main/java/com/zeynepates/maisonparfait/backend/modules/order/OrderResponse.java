package com.zeynepates.maisonparfait.backend.modules.order;

import com.zeynepates.maisonparfait.backend.modules.shipping.ShippingAddressResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrderResponse(

        UUID orderId,
        OrderStatus status,
        BigDecimal totalAmount,
        String currency,
        LocalDateTime createdAt,
        List<OrderItemResponse> items,
        ShippingAddressResponse shippingAddress
) {
}
