package com.zeynepates.maisonparfait.backend.modules.product;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class ProductInMemoryStore {

    private final Map<Long, Product> store = new ConcurrentHashMap<>();
    private final AtomicLong seq = new AtomicLong(1);

    public Product save(Product product) {
        if (product.getId() == null) {
            product.setId(seq.getAndIncrement());
        }
        store.put(product.getId(), product);
        return product;
    }

    public Product findById(Long id) {
        return store.get(id);
    }

    public Map<Long, Product> findAll() {
        return store;
    }
}
