package com.zeynepates.maisonparfait.backend.modules.payment;

import com.zeynepates.maisonparfait.backend.modules.common.entity.BaseEntity;
import com.zeynepates.maisonparfait.backend.modules.order.Order;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "payments")
public class Payment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "provider", length = 50, nullable = false)
    private String provider;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50, nullable = false)
    private PaymentStatus status;

    @Column(name = "amount_cents", nullable = false)
    private Long amountCents;

    @Column(name = "currency", length = 3, nullable = false)
    private String currency = "TRY";

    @Column(name = "provider_payment_id", length = 100)
    private String providerPaymentId;

    @Column(name = "idempotency_key", length = 100)
    private String idempotencyKey;

    @Column(name = "failure_reason", columnDefinition = "text")
    private String failureReason;

    @Column(name = "paid_at")
    private OffsetDateTime paidAt;
}
