package com.zeynepates.maisonparfait.backend.modules.payment;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class Payment {
    private UUID id;
    private UUID orderId;
    private PaymentStatus status;
    private BigDecimal amount;
    private String currency;
    private LocalDateTime createdAt;

    public Payment(UUID orderId, BigDecimal amount, String currency) {
        this.id = UUID.randomUUID();
        this.orderId = orderId;
        this.amount = amount;
        this.currency = currency;
        this.status = PaymentStatus.INITIATED;
        this.createdAt = LocalDateTime.now();
    }
}
