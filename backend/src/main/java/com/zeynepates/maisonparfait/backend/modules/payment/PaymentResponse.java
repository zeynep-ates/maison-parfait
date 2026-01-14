package com.zeynepates.maisonparfait.backend.modules.payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PaymentResponse(
        UUID paymentId,
        UUID orderId,
        PaymentStatus status,
        BigDecimal amount,
        String currency,
        LocalDateTime createdAt
) {
}
