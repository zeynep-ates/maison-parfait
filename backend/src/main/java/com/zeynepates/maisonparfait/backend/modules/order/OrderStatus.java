package com.zeynepates.maisonparfait.backend.modules.order;

public enum OrderStatus {

    CREATED,
    PAYMENT_PENDING,
    PAID,
    PREPARING,
    SHIPPED,
    DELIVERED,

    CANCELLED,
    REFUND_REQUESTED,
    REFUNDED
}
