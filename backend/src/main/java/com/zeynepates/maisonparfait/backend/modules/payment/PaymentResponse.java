package com.zeynepates.maisonparfait.backend.modules.payment;

public record PaymentResponse(
        Long paymentId,
        Long orderId,
        PaymentStatus status,
        Long amountCents,
        String currency,
        java.time.OffsetDateTime createdAt
) {
}
