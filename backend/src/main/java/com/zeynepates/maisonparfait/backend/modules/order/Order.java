package com.zeynepates.maisonparfait.backend.modules.order;

import com.zeynepates.maisonparfait.backend.modules.shipping.ShippingAddressResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class Order {

    private UUID id;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private String currency;
    private LocalDateTime createdAt;
    private Long shippingAddressId;
    private List<OrderItem> items = new ArrayList<>();
    private ShippingAddressResponse shippingSnapshot;

    public Order() {
        this.id = UUID.randomUUID();
        this.createdAt = LocalDateTime.now();
    }
}
