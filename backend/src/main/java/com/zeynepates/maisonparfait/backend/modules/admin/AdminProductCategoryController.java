package com.zeynepates.maisonparfait.backend.modules.admin;

import com.zeynepates.maisonparfait.backend.common.exception.ConflictException;
import com.zeynepates.maisonparfait.backend.common.exception.NotFoundException;
import com.zeynepates.maisonparfait.backend.modules.category.Category;
import com.zeynepates.maisonparfait.backend.modules.category.CategoryRepository;
import com.zeynepates.maisonparfait.backend.modules.category.CategoryResponse;
import com.zeynepates.maisonparfait.backend.modules.product.Product;
import com.zeynepates.maisonparfait.backend.modules.product.ProductRepository;
import com.zeynepates.maisonparfait.backend.modules.product.category.ProductCategory;
import com.zeynepates.maisonparfait.backend.modules.product.category.ProductCategoryId;
import com.zeynepates.maisonparfait.backend.modules.product.category.ProductCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/products/{productId}/categories")
public class AdminProductCategoryController {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductCategoryRepository productCategoryRepository;

    // Ürüne bağlı kategorileri listele
    @GetMapping
    public List<CategoryResponse> list(@PathVariable Long productId) {
        ensureProductExists(productId);

        return productCategoryRepository.findAllByProductIdOrderByIdAsc(productId).stream()
                .map(ProductCategory::getCategory)
                .map(CategoryResponse::from)
                .toList();
    }

    // Ürüne kategori ekle
    @PostMapping("/{categoryId}")
    @ResponseStatus(HttpStatus.CREATED)
    public void attach(@PathVariable Long productId, @PathVariable Long categoryId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found: " + productId));

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Category not found: " + categoryId));

        if (productCategoryRepository.existsByProductIdAndCategoryId(productId, categoryId)) {
            throw new ConflictException("Product already has category: " + categoryId);
        }

        ProductCategory pc = new ProductCategory();
        pc.setId(new ProductCategoryId(productId, categoryId));
        pc.setProduct(product);
        pc.setCategory(category);

        productCategoryRepository.save(pc);
    }

    // Üründen kategori kaldır
    @DeleteMapping("/{categoryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void detach(@PathVariable Long productId, @PathVariable Long categoryId) {
        ensureProductExists(productId);

        ProductCategory pc = productCategoryRepository.findByProductIdAndCategoryId(productId, categoryId)
                .orElseThrow(() -> new NotFoundException("Category not attached to product: " + categoryId));

        productCategoryRepository.delete(pc);
    }

    private void ensureProductExists(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new NotFoundException("Product not found: " + productId);
        }
    }
}
