package com.zeynepates.maisonparfait.backend.modules.product;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    @GetMapping("/{id}")
    public ProductResponse get(@PathVariable Long id) {
        return productService.get(id, true);
    }

    @GetMapping
    public Page<ProductResponse> list(Pageable pageable) {
        return productService.list(true, pageable);
    }
}
