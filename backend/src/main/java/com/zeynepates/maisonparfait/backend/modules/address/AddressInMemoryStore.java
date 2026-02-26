package com.zeynepates.maisonparfait.backend.modules.address;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class AddressInMemoryStore {

    private final Map<Long, Address> store = new ConcurrentHashMap<>();
    private final AtomicLong seq = new AtomicLong(1);

    public Address save(Address address) {
        if (address.getId() == null) {
            address.setId(seq.getAndIncrement());
        }
        store.put(address.getId(), address);
        return address;
    }

    public Address findById(Long id) {
        return store.get(id);
    }
}
