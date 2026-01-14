package com.zeynepates.maisonparfait.backend.modules.order;

import com.zeynepates.maisonparfait.backend.common.exception.ConflictException;
import com.zeynepates.maisonparfait.backend.common.exception.NotFoundException;
import com.zeynepates.maisonparfait.backend.modules.payment.Payment;
import com.zeynepates.maisonparfait.backend.modules.payment.PaymentInMemoryStore;
import com.zeynepates.maisonparfait.backend.modules.payment.PaymentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderPaymentService {

    private final OrderInMemoryStore orderStore;
    private final PaymentInMemoryStore paymentStore;

    public PaymentResponse initiatePayment(UUID orderId) {

        Order order = orderStore.findById(orderId);
        if (order == null) {
            throw new NotFoundException("Order not found: " + orderId);
        }

        if (order.getStatus() != OrderStatus.CREATED) {
            throw new ConflictException("Payment can be initiated only when order status is CREATED. Current: " + order.getStatus());
        }

        // (opsiyonel) aynı order için tekrar initiate edilmesin
        if (paymentStore.existsInitiatedForOrder(orderId)) {
            throw new ConflictException("Payment already initiated for order: " + orderId);
        }

        order.setStatus(OrderStatus.PAYMENT_PENDING);
        orderStore.save(order);

        Payment payment = new Payment(orderId, order.getTotalAmount(), order.getCurrency());
        paymentStore.save(payment);

        return new PaymentResponse(
                payment.getId(),
                payment.getOrderId(),
                payment.getStatus(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getCreatedAt()
        );
    }
}
