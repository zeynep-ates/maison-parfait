package com.zeynepates.maisonparfait.backend.modules.order;

import com.zeynepates.maisonparfait.backend.modules.address.Address;
import com.zeynepates.maisonparfait.backend.modules.common.entity.BaseEntity;
import com.zeynepates.maisonparfait.backend.modules.user.User;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "orders")
public class Order extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "shipping_address_id", nullable = false)
    private Address address;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50, nullable = false)
    private OrderStatus status;

    @Column(name = "currency", length = 3, nullable = false)
    private String currency = "TRY";

    @Column(name = "subtotal_cents", nullable = false)
    private Long subtotalCents = 0L;

    @Column(name = "shipping_cents", nullable = false)
    private Long shippingCents = 0L;

    @Column(name = "discount_cents", nullable = false)
    private Long discountCents = 0L;

    @Column(name = "tax_cents", nullable = false)
    private Long taxCents = 0L;

    @Column(name = "total_cents", nullable = false)
    private Long totalCents = 0L;
}
