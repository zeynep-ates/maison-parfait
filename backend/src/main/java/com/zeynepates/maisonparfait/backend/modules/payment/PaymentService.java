package com.zeynepates.maisonparfait.backend.modules.payment;

import com.zeynepates.maisonparfait.backend.common.exception.ConflictException;
import com.zeynepates.maisonparfait.backend.common.exception.NotFoundException;
import com.zeynepates.maisonparfait.backend.modules.order.Order;
import com.zeynepates.maisonparfait.backend.modules.order.OrderInMemoryStore;
import com.zeynepates.maisonparfait.backend.modules.order.OrderResponse;
import com.zeynepates.maisonparfait.backend.modules.order.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentInMemoryStore paymentStore;
    private final OrderInMemoryStore orderStore;

    public PaymentResponse confirmPayment(UUID paymentId, PaymentResult result) {

        Payment payment = paymentStore.findById(paymentId);
        if (payment == null) {
            throw new NotFoundException("Payment not found: " + paymentId);
        }
        if (payment.getStatus() != PaymentStatus.INITIATED) {
            throw new ConflictException("Payment cannot be confirmed in status: " + payment.getStatus());
        }

        Order order = orderStore.findById(payment.getOrderId());
        if (order == null) {
            throw new NotFoundException("Order not found for payment: " + payment.getOrderId());
        }

        if (result == PaymentResult.success) {
            payment.setStatus(PaymentStatus.SUCCESS);
            order.setStatus(OrderStatus.PAID);
            orderStore.save(order);
        } else {
            payment.setStatus(PaymentStatus.FAILED);
        }

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

    public PaymentResponse getPaymentById(UUID paymentId) {
        Payment payment = paymentStore.findById(paymentId);
        if (payment == null) {
            throw new NotFoundException("Payment not found: " + paymentId);
        }
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
