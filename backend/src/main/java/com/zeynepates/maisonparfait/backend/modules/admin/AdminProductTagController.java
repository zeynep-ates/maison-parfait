package com.zeynepates.maisonparfait.backend.modules.admin;

import com.zeynepates.maisonparfait.backend.common.exception.ConflictException;
import com.zeynepates.maisonparfait.backend.common.exception.NotFoundException;
import com.zeynepates.maisonparfait.backend.modules.product.Product;
import com.zeynepates.maisonparfait.backend.modules.product.ProductRepository;
import com.zeynepates.maisonparfait.backend.modules.product.tag.ProductTag;
import com.zeynepates.maisonparfait.backend.modules.product.tag.ProductTagId;
import com.zeynepates.maisonparfait.backend.modules.product.tag.ProductTagRepository;
import com.zeynepates.maisonparfait.backend.modules.tag.Tag;
import com.zeynepates.maisonparfait.backend.modules.tag.TagRepository;
import com.zeynepates.maisonparfait.backend.modules.tag.TagResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/products/{productId}/tags")
public class AdminProductTagController {

    private final ProductRepository productRepository;
    private final TagRepository tagRepository;
    private final ProductTagRepository productTagRepository;

    @GetMapping
    public List<TagResponse> list(@PathVariable Long productId) {
        ensureProductExists(productId);

        return productTagRepository.findAllByProductIdOrderByIdAsc(productId).stream()
                .map(ProductTag::getTag)
                .map(TagResponse::from)
                .toList();
    }

    @PostMapping("/{tagId}")
    @ResponseStatus(HttpStatus.CREATED)
    public void attach(@PathVariable Long productId, @PathVariable Long tagId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found: " + productId));

        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new NotFoundException("Tag not found: " + tagId));

        if (productTagRepository.existsByProductIdAndTagId(productId, tagId)) {
            throw new ConflictException("Product already has tag: " + tagId);
        }

        ProductTag pt = new ProductTag();
        pt.setId(new ProductTagId(productId, tagId));
        pt.setProduct(product);
        pt.setTag(tag);

        productTagRepository.save(pt);
    }

    @DeleteMapping("/{tagId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void detach(@PathVariable Long productId, @PathVariable Long tagId) {
        ensureProductExists(productId);

        ProductTag pt = productTagRepository.findByProductIdAndTagId(productId, tagId)
                .orElseThrow(() -> new NotFoundException("Tag not attached to product: " + tagId));

        productTagRepository.delete(pt);
    }

    private void ensureProductExists(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new NotFoundException("Product not found: " + productId);
        }
    }
}
