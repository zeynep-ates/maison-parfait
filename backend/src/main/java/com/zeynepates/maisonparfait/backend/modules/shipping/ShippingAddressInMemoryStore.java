package com.zeynepates.maisonparfait.backend.modules.shipping;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class ShippingAddressInMemoryStore {

    private final Map<Long, ShippingAddress> store = new ConcurrentHashMap<>();
    private final AtomicLong seq = new AtomicLong(1);

    public ShippingAddress save(ShippingAddress address) {
        if (address.getId() == null) {
            address.setId(seq.getAndIncrement());
        }
        store.put(address.getId(), address);
        return address;
    }

    public ShippingAddress findById(Long id) {
        return store.get(id);
    }
}
