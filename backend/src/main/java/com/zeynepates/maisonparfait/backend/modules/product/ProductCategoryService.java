package com.zeynepates.maisonparfait.backend.modules.product;

import com.zeynepates.maisonparfait.backend.common.exception.NotFoundException;
import com.zeynepates.maisonparfait.backend.modules.category.Category;
import com.zeynepates.maisonparfait.backend.modules.category.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductCategoryService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductCategoryRepository productCategoryRepository;

    @Transactional
    public void addCategoryToProduct(Long productId, Long categoryId) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found: " + productId));

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Category not found: " + categoryId));

        ProductCategoryId id = new ProductCategoryId(productId, categoryId);

        if (productCategoryRepository.existsById(id)) {
            return;
        }

        ProductCategory pc = new ProductCategory();
        pc.setId(id);
        pc.setProduct(product);
        pc.setCategory(category);

        productCategoryRepository.save(pc);
    }

    @Transactional
    public void removeCategoryFromProduct(Long productId, Long categoryId) {

        ProductCategoryId id = new ProductCategoryId(productId, categoryId);

        if (!productCategoryRepository.existsById(id)) {
            return;
        }

        productCategoryRepository.deleteById(id);
    }
}
