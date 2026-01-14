package com.zeynepates.maisonparfait.backend.modules.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateOrderRequest(

        @NotEmpty(message = "Order must contain at least one item")
        List<@Valid OrderItemRequest> items,

        @NotNull(message = "Shipping address id is required")
        Long shippingAddressId
) {
}
