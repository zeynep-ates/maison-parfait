package com.zeynepates.maisonparfait.backend.modules.product;

import com.zeynepates.maisonparfait.backend.common.exception.ConflictException;
import com.zeynepates.maisonparfait.backend.common.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    @Transactional
    public ProductResponse create(CreateProductRequest req) {
        if (productRepository.existsBySku(req.sku())) {
            throw new ConflictException("SKU already exists: " + req.sku());
        }

        Product p = new Product();
        p.setSku(req.sku());
        p.setName(req.name());
        p.setDescription(req.description());
        p.setPriceCents(req.priceCents());
        p.setCurrency(req.currency() != null ? req.currency() : "TRY");
        p.setStock(req.stock() != null ? req.stock() : 0);
        if (req.isActive() != null) p.setIsActive(req.isActive());
        if (req.isPerishable() != null) p.setIsPerishable(req.isPerishable());
        p.setShelfLifeDays(req.shelfLifeDays());
        p.setWeightGrams(req.weightGrams());

        return toResponse(productRepository.save(p));
    }

    @Transactional(readOnly = true)
    public ProductResponse get(Long id, boolean onlyActive) {
        return toResponse(findOrThrow(id, onlyActive));
    }

    @Transactional(readOnly = true)
    public Product getByIdOrThrow(Long id, boolean onlyActive) {
        return findOrThrow(id, onlyActive);
    }

    private Product findOrThrow(Long id, boolean onlyActive) {
        if (onlyActive) {
            return productRepository.findByIdAndIsActiveTrue(id)
                    .orElseThrow(() -> new NotFoundException("Product not found: " + id));
        }

        return productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found: " + id));
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> list(Boolean active, Pageable pageable) {
        Page<Product> page;
        if (active == null) page = productRepository.findAll(pageable);
        else if (active) page = productRepository.findAllByIsActiveTrue(pageable);
        else page = productRepository.findAllByIsActiveFalse(pageable);

        return page.map(this::toResponse);
    }

    @Transactional
    public ProductResponse update(Long id, UpdateProductRequest req) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found: " + id));

        if (req.sku() != null && !req.sku().equals(p.getSku())) {
            if (productRepository.existsBySku(req.sku())) {
                throw new ConflictException("SKU already exists: " + req.sku());
            }
            p.setSku(req.sku());
        }

        if (req.name() != null) p.setName(req.name());
        if (req.description() != null) p.setDescription(req.description());
        if (req.priceCents() != null) p.setPriceCents(req.priceCents());
        if (req.currency() != null) p.setCurrency(req.currency());
        if (req.stock() != null) p.setStock(req.stock());
        if (req.isActive() != null) p.setIsActive(req.isActive());
        if (req.isPerishable() != null) p.setIsPerishable(req.isPerishable());
        if (req.shelfLifeDays() != null) p.setShelfLifeDays(req.shelfLifeDays());
        if (req.weightGrams() != null) p.setWeightGrams(req.weightGrams());

        return toResponse(p);
    }

    @Transactional
    public void delete(Long id) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found: " + id));

        p.setIsActive(false);
    }


    private ProductResponse toResponse(Product p) {
        return new ProductResponse(
                p.getId(),
                p.getSku(),
                p.getName(),
                p.getDescription(),
                p.getPriceCents(),
                p.getCurrency(),
                p.getStock(),
                p.getIsActive(),
                p.getIsPerishable(),
                p.getShelfLifeDays(),
                p.getWeightGrams()
        );
    }
}
