package com.zeynepates.backend.modules.order;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class OrderInMemoryStore {

    private final Map<UUID, Order> store = new ConcurrentHashMap<>();

    public Order save(Order order) {
        store.put(order.getId(), order);
        return order;
    }

    public Order findById(UUID id) {
        return store.get(id);
    }

//    public boolean exists(UUID id) {
//        return store.containsKey(id);
//    }

    public void delete(UUID id) {
        store.remove(id);
    }

    public Map<UUID, Order> findAll() {
        return store;
    }
}
