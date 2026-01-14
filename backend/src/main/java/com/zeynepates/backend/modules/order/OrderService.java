package com.zeynepates.backend.modules.order;

import com.zeynepates.backend.modules.product.Product;
import com.zeynepates.backend.modules.product.ProductService;
import com.zeynepates.backend.modules.shipping.ShippingAddressResponse;
import com.zeynepates.backend.modules.shipping.ShippingAddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderInMemoryStore orderStore;
    private final ProductService productService;
    private final ShippingAddressService shippingAddressService;

    public OrderResponse createOrder(CreateOrderRequest request) {

        // 1) shipping snapshot al
        ShippingAddressResponse snapshot = shippingAddressService.getSnapshot(request.shippingAddressId());

        // 2) item'ları product'tan besle
        List<OrderItem> orderItems = request.items().stream().map(i -> {
            Product p = productService.getByIdOrThrow(i.productId());
            return new OrderItem(
                    p.getId(),
                    p.getName(),
                    i.quantity(),
                    p.getPrice()
            );
        }).toList();

        // 3) totalAmount hesapla
        BigDecimal totalAmount = orderItems.stream()
                .map(OrderItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 4) order oluştur + kaydet
        Order order = new Order();
        order.setStatus(OrderStatus.CREATED);
        order.setItems(orderItems);
        order.setTotalAmount(totalAmount);
        order.setCurrency("TRY"); // istersen product currency kontrolü ekleriz
        order.setShippingSnapshot(snapshot);

        orderStore.save(order);

        // 5) response mapping
        List<OrderItemResponse> itemResponses = orderItems.stream()
                .map(oi -> new OrderItemResponse(
                        oi.getProductId(),
                        oi.getProductName(),
                        oi.getQuantity(),
                        oi.getUnitPrice(),
                        oi.getTotalPrice()
                ))
                .toList();

        return new OrderResponse(
                order.getId(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getCurrency(),
                order.getCreatedAt(),
                itemResponses,
                snapshot
        );
    }
}
