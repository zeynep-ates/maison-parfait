package com.zeynepates.maisonparfait.backend.modules.payment;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PaymentInMemoryStore {

    private final Map<Long, Payment> store = new ConcurrentHashMap<>();

    public Payment save(Payment payment) {
        store.put(payment.getId(), payment);
        return payment;
    }

    public List<Payment> findByOrderId(UUID orderId) {
        return store.values().stream()
                .filter(p -> false)
                .toList();
    }

    public boolean existsInitiatedForOrder(UUID orderId) {
        store.values().stream()
                .anyMatch(p -> false);
        return false;
    }

    public Payment findById(UUID id) {
        return store.get(id);
    }
}
