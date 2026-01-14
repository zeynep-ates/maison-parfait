package com.zeynepates.maisonparfait.backend.modules.order;

import com.zeynepates.maisonparfait.backend.common.exception.ConflictException;
import com.zeynepates.maisonparfait.backend.common.exception.NotFoundException;
import com.zeynepates.maisonparfait.backend.modules.product.Product;
import com.zeynepates.maisonparfait.backend.modules.product.ProductService;
import com.zeynepates.maisonparfait.backend.modules.shipping.ShippingAddressResponse;
import com.zeynepates.maisonparfait.backend.modules.shipping.ShippingAddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderInMemoryStore orderStore;
    private final ProductService productService;
    private final ShippingAddressService shippingAddressService;

    public OrderResponse createOrder(CreateOrderRequest request) {

        ShippingAddressResponse snapshot = shippingAddressService.getSnapshot(request.shippingAddressId());

        List<OrderItem> orderItems = request.items().stream().map(i -> {
            Product p = productService.getByIdOrThrow(i.productId());
            return new OrderItem(
                    p.getId(),
                    p.getName(),
                    i.quantity(),
                    p.getPrice()
            );
        }).toList();

        BigDecimal totalAmount = orderItems.stream()
                .map(OrderItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Order order = new Order();
        order.setStatus(OrderStatus.CREATED);
        order.setItems(orderItems);
        order.setTotalAmount(totalAmount);
        order.setCurrency("TRY");
        order.setShippingSnapshot(snapshot);

        orderStore.save(order);

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

    public List<OrderResponse> getAllOrders() {
        return orderStore.findAll()
                .values()
                .stream()
                .map(this::toOrderResponse)
                .sorted(Comparator.comparing(OrderResponse::createdAt).reversed())
                .toList();
    }

    public OrderResponse getOrderById(UUID orderId) {
        Order order = orderStore.findById(orderId);
        if (order == null) {
            throw new NotFoundException("Order not found: " + orderId);
        }
        return toOrderResponse(order);
    }

    public OrderResponse cancelOrder(UUID orderId) {
        Order order = orderStore.findById(orderId);
        if (order == null) {
            throw new NotFoundException("Order not found: " + orderId);
        }

        if (order.getStatus() != OrderStatus.CREATED &&
        order.getStatus() != OrderStatus.PAYMENT_PENDING) {
            throw new ConflictException("Order cannot be cancelled in status: " + order.getStatus());
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderStore.save(order);
        return toOrderResponse(order);
    }

    public OrderResponse markPaymentPending(UUID orderId) {
        Order order = orderStore.findById(orderId);
        if (order == null) {
            throw new NotFoundException("Order not found: " + orderId);
        }

        if (order.getStatus() != OrderStatus.CREATED) {
            throw new ConflictException("Order cannot be marked payment pending in status: " + order.getStatus());
        }

        order.setStatus(OrderStatus.PAYMENT_PENDING);
        orderStore.save(order);
        return toOrderResponse(order);
    }

    private OrderResponse toOrderResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(i -> new OrderItemResponse(
                        i.getProductId(),
                        i.getProductName(),
                        i.getQuantity(),
                        i.getUnitPrice(),
                        i.getTotalPrice()
                )).toList();

        return new OrderResponse(
                order.getId(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getCurrency(),
                order.getCreatedAt(),
                itemResponses,
                order.getShippingSnapshot()
        );
    }
}
