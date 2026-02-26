package com.zeynepates.maisonparfait.backend.modules.product;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse create(@Valid @RequestBody CreateProductRequest req) {
        return productService.create(req);
    }

    @GetMapping("/{id}")
    public ProductResponse get(@PathVariable Long id,
                               @RequestParam(defaultValue = "true") boolean onlyActive) {
        return productService.get(id, onlyActive);
    }

    @GetMapping
    public Page<ProductResponse> list(
            @RequestParam(required = false) Boolean active,
            Pageable pageable
    ) {
        return productService.list(active, pageable);
    }

    @PutMapping("/{id}")
    public ProductResponse update(@PathVariable Long id,
                                  @Valid @RequestBody UpdateProductRequest req) {
        return productService.update(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        productService.delete(id);
    }

}
