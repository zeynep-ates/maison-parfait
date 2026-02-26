package com.zeynepates.maisonparfait.backend.modules.product;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductCategoryController {

    private final ProductCategoryService productCategoryService;

    @PostMapping("/{productId}/categories/{categoryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void addCategory(
            @PathVariable Long productId,
            @PathVariable Long categoryId
    ) {
        productCategoryService.addCategoryToProduct(productId, categoryId);
    }

    @DeleteMapping("/{productId}/categories/{categoryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeCategory(
            @PathVariable Long productId,
            @PathVariable Long categoryId
    ) {
        productCategoryService.removeCategoryFromProduct(productId, categoryId);
    }
}
