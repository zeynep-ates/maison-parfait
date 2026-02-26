package com.zeynepates.maisonparfait.backend.modules.order;

import com.zeynepates.maisonparfait.backend.modules.address.AddressResponse;

import java.time.OffsetDateTime;
import java.util.List;

public record OrderResponse(

        Long orderId,
        OrderStatus status,
        Long totalAmountCents,
        String currency,
        OffsetDateTime createdAt,
        List<OrderItemResponse> items,
        AddressResponse address
) {
}
