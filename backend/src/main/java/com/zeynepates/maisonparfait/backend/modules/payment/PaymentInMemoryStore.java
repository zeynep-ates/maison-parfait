package com.zeynepates.maisonparfait.backend.modules.payment;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PaymentInMemoryStore {

    private final Map<UUID, Payment> store = new ConcurrentHashMap<>();

    public Payment save(Payment payment) {
        store.put(payment.getId(), payment);
        return payment;
    }

    public List<Payment> findByOrderId(UUID orderId) {
        return store.values().stream()
                .filter(p -> p.getOrderId().equals(orderId))
                .toList();
    }

    public boolean existsInitiatedForOrder(UUID orderId) {
        return store.values().stream()
                .anyMatch(p -> p.getOrderId().equals(orderId));
    }

    public Payment findById(UUID id) {
        return store.get(id);
    }
}
