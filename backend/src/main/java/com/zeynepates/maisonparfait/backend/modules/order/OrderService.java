package com.zeynepates.maisonparfait.backend.modules.order;

import com.zeynepates.maisonparfait.backend.common.exception.ConflictException;
import com.zeynepates.maisonparfait.backend.common.exception.NotFoundException;
import com.zeynepates.maisonparfait.backend.modules.product.Product;
import com.zeynepates.maisonparfait.backend.modules.product.ProductService;
import com.zeynepates.maisonparfait.backend.modules.address.Address;
import com.zeynepates.maisonparfait.backend.modules.address.AddressResponse;
import com.zeynepates.maisonparfait.backend.modules.address.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductService productService;
    private final AddressService addressService;
    private final OrderItemRepository orderItemRepository;
    private final OrderAddressRepository orderAddressRepository;

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {

        // 1) Shipping address (addresses tablosundan)
        Address address = addressService.getEntity(request.shippingAddressId());

        // 2) Order oluştur
        Order order = new Order();
        order.setStatus(OrderStatus.CREATED);
        order.setCurrency("TRY");
        order.setAddress(address);

        // totals başlangıç
        order.setSubtotalCents(0L);
        order.setShippingCents(0L);
        order.setDiscountCents(0L);
        order.setTaxCents(0L);
        order.setTotalCents(0L);

        Order finalOrder = orderRepository.save(order);
        List<OrderItem> orderItems = request.items().stream().map(i -> {
            Product p = productService.getByIdOrThrow(i.productId(), true);

            OrderItem oi = new OrderItem();
            oi.setOrder(finalOrder);
            oi.setProduct(p);
            oi.setVariant(null); // şimdilik yoksa
            oi.setQuantity(i.quantity());
            oi.setUnitPriceCents(p.getPriceCents());
            oi.setCurrency(finalOrder.getCurrency());
            return oi;
        }).toList();

        orderItemRepository.saveAll(orderItems);

        // 4) Totalleri hesapla (subtotal = item toplamı)
        long subtotal = orderItems.stream()
                .mapToLong(oi -> oi.getUnitPriceCents() * (long) oi.getQuantity())
                .sum();

        order.setSubtotalCents(subtotal);
        order.setTotalCents(subtotal); // şimdilik shipping/discount/tax yok

        // 5) Shipping snapshot’ı order_addresses tablosuna yaz
        // (addresses tablosu değişse bile order’da snapshot kalsın diye)
        OrderAddress snapshot = new OrderAddress();
        snapshot.setOrder(order);
        snapshot.setType(OrderAddressType.SHIPPING);
        snapshot.setFullName(address.getFullName());
        snapshot.setPhone(address.getPhone());
        snapshot.setCountry(address.getCountry());
        snapshot.setCity(address.getCity());
        snapshot.setDistrict(address.getDistrict());
        snapshot.setAddressLine(address.getAddressLine());
        snapshot.setPostalCode(address.getPostalCode());
        orderAddressRepository.save(snapshot);

        // order update (dirty checking)
        // orderRepository.save(order); // @Transactional olduğu için şart değil

        // 6) Response (eski DTO’larına benzeterek)
        AddressResponse addressSnapshotResp = new AddressResponse(
                address.getId(),
                address.getTitle(),
                address.getFullName(),
                address.getPhone(),
                address.getCountry(),
                address.getCity(),
                address.getDistrict(),
                address.getAddressLine(),
                address.getPostalCode()
        );

        List<OrderItemResponse> itemResponses = orderItems.stream()
                .map(oi -> new OrderItemResponse(
                        oi.getProduct().getId(),
                        oi.getProduct().getName(),
                        oi.getQuantity(),
                        oi.getUnitPriceCents(),
                        oi.getUnitPriceCents() * (long) oi.getQuantity()
                ))
                .toList();

        // Senin OrderResponse field’ların: id, status, totalAmountCents, currency, createdAt, items, snapshot
        return new OrderResponse(
                order.getId(),
                order.getStatus(),
                order.getTotalCents(),      // eski totalAmountCents karşılığı
                order.getCurrency(),
                order.getCreatedAt(),
                itemResponses,
                addressSnapshotResp
        );
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream()
                .map(this::toOrderResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found: " + orderId));
        return toOrderResponse(order);
    }

    @Transactional
    public OrderResponse cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found: " + orderId));

        if (order.getStatus() != OrderStatus.CREATED &&
                order.getStatus() != OrderStatus.PAYMENT_PENDING) {
            throw new ConflictException("Order cannot be cancelled in status: " + order.getStatus());
        }

        order.setStatus(OrderStatus.CANCELLED);
        return toOrderResponse(order);
    }

    @Transactional
    public OrderResponse markPaymentPending(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found: " + orderId));

        if (order.getStatus() != OrderStatus.CREATED) {
            throw new ConflictException("Order cannot be marked payment pending in status: " + order.getStatus());
        }

        order.setStatus(OrderStatus.PAYMENT_PENDING);
        return toOrderResponse(order);
    }

    private OrderResponse toOrderResponse(Order order) {

        // order_items’ı çek (Order entity’de list yoksa repo ile çek)
        List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());

        List<OrderItemResponse> itemResponses = items.stream()
                .map(i -> new OrderItemResponse(
                        i.getProduct().getId(),
                        i.getProduct().getName(),
                        i.getQuantity(),
                        i.getUnitPriceCents(),
                        i.getUnitPriceCents() * (long) i.getQuantity()
                )).toList();

        // shipping snapshot’ı çek
        OrderAddress shippingSnapshot = orderAddressRepository
                .findFirstByOrder_IdAndType(order.getId(), OrderAddressType.SHIPPING)
                .orElse(null);

        AddressResponse snapshotResp = null;
        if (shippingSnapshot != null) {
            snapshotResp = new AddressResponse(
                    null, // snapshot id’yi dönmek zorunda değilsin
                    null,
                    shippingSnapshot.getFullName(),
                    shippingSnapshot.getPhone(),
                    shippingSnapshot.getCountry(),
                    shippingSnapshot.getCity(),
                    shippingSnapshot.getDistrict(),
                    shippingSnapshot.getAddressLine(),
                    shippingSnapshot.getPostalCode()
            );
        }

        return new OrderResponse(
                order.getId(),
                order.getStatus(),
                order.getTotalCents(),
                order.getCurrency(),
                order.getCreatedAt(),
                itemResponses,
                snapshotResp
        );
    }
}
