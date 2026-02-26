package com.zeynepates.maisonparfait.backend.modules.payment;

import com.zeynepates.maisonparfait.backend.common.exception.ConflictException;
import com.zeynepates.maisonparfait.backend.common.exception.NotFoundException;
import com.zeynepates.maisonparfait.backend.modules.order.Order;
import com.zeynepates.maisonparfait.backend.modules.order.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;

    @Transactional
    public PaymentResponse confirmPayment(Long paymentId, PaymentResult result) {

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new NotFoundException("Payment not found: " + paymentId));

        if (payment.getStatus() != PaymentStatus.INITIATED) {
            throw new ConflictException("Payment cannot be confirmed in status: " + payment.getStatus());
        }

        // payment -> order ilişkisinden direkt gelir
        Order order = payment.getOrder();
        if (order == null) {
            // normalde olmaz (DB constraint var), ama güvenlik:
            throw new NotFoundException("Order not found for payment: " + paymentId);
        }

        if (result == PaymentResult.SUCCESS) {
            payment.setStatus(PaymentStatus.SUCCEEDED);
            order.setStatus(OrderStatus.PAID);
        } else {
            payment.setStatus(PaymentStatus.FAILED);
        }

        // transactional olduğu için save şart değil ama net olsun diye:
        paymentRepository.save(payment);
        // orderRepository.save(order); // istersen yaz, ama dirty-checking yeter

        return toResponse(payment);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new NotFoundException("Payment not found: " + paymentId));
        return toResponse(payment);
    }

    private PaymentResponse toResponse(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getOrder().getId(),
                payment.getStatus(),
                payment.getAmountCents(),
                payment.getCurrency(),
                payment.getCreatedAt()
        );
    }
}