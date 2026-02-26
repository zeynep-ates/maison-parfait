package com.zeynepates.maisonparfait.backend.modules.order;

import com.zeynepates.maisonparfait.backend.common.exception.ConflictException;
import com.zeynepates.maisonparfait.backend.common.exception.NotFoundException;
import com.zeynepates.maisonparfait.backend.modules.payment.Payment;
import com.zeynepates.maisonparfait.backend.modules.payment.PaymentRepository;
import com.zeynepates.maisonparfait.backend.modules.payment.PaymentResponse;
import com.zeynepates.maisonparfait.backend.modules.payment.PaymentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderPaymentService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;

    @Transactional
    public PaymentResponse initiatePayment(Long orderId) {

        // 1) Order var mı?
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found: " + orderId));

        // 2) Daha önce initiate edildi mi? (idempotency)
        if (paymentRepository.existsByOrder_Id(orderId)) {
            throw new ConflictException("Payment already initiated for order: " + orderId);
        }

        // 3) Status kontrol
        if (order.getStatus() != OrderStatus.CREATED) {
            throw new ConflictException("Payment cannot be initiated in status: " + order.getStatus());
        }

        // 4) Order status -> PAYMENT_PENDING
        order.setStatus(OrderStatus.PAYMENT_PENDING);

        // 5) Payment oluştur (constructor yok → setter ile)
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setStatus(PaymentStatus.INITIATED);
        payment.setAmountCents(order.getTotalCents());
        payment.setCurrency(order.getCurrency());

        paymentRepository.save(payment);

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