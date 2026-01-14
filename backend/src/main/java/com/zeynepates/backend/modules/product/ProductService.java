package com.zeynepates.backend.modules.product;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductInMemoryStore store;

    public ProductResponse create(CreateProductRequest req) {
        Product saved = store.save(new Product(null, req.name(), req.price(), req.currency()));
        return toResponse(saved);
    }

    public Product getByIdOrThrow(Long id) {
        Product p = store.findById(id);
        if (p == null) throw new RuntimeException("Product not found: " + id);
        return p;
    }

    public ProductResponse get(Long id) {
        return toResponse(getByIdOrThrow(id));
    }

    private ProductResponse toResponse(Product p) {
        return new ProductResponse(p.getId(), p.getName(), p.getPrice(), p.getCurrency());
    }
}
