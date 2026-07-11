package com.zeynepates.maisonparfait.backend.modules.admin;

import com.zeynepates.maisonparfait.backend.common.exception.NotFoundException;
import com.zeynepates.maisonparfait.backend.modules.product.*;
import com.zeynepates.maisonparfait.backend.modules.product.image.ProductImage;
import com.zeynepates.maisonparfait.backend.modules.product.image.ProductImageRepository;
import com.zeynepates.maisonparfait.backend.modules.product.image.ProductImageResponse;
import com.zeynepates.maisonparfait.backend.modules.product.image.ProductImageStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/products/{productId}/images")
public class AdminProductImageController {

    private final ProductRepository productRepository;
    private final ProductImageRepository imageRepository;
    private final ProductImageStorage storage;

    // Listele (admin)
    @GetMapping
    public List<ProductImageResponse> list(@PathVariable Long productId) {
        ensureProductExists(productId);
        return imageRepository.findAllByProductIdOrderBySortOrderAscIdAsc(productId)
                .stream()
                .map(ProductImageResponse::from)
                .toList();
    }

    // Upload
    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    @ResponseStatus(HttpStatus.CREATED)
    public ProductImageResponse upload(
            @PathVariable Long productId,
            @RequestPart("file") MultipartFile file,
            @RequestParam(required = false) String altText,
            @RequestParam(required = false) Integer sortOrder,
            @RequestParam(required = false) Boolean isPrimary
    ) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found: " + productId));

        String publicUrl = storage.saveProductImage(productId, file);

        if (Boolean.TRUE.equals(isPrimary)) {
            imageRepository.findAllByProductIdOrderBySortOrderAscIdAsc(productId)
                    .forEach(img -> {
                        if (Boolean.TRUE.equals(img.getIsPrimary())) img.setIsPrimary(false);
                    });
        }

        ProductImage img = new ProductImage();
        img.setProduct(product);
        img.setUrl(publicUrl);
        img.setAltText(altText);
        img.setSortOrder(sortOrder != null ? sortOrder : 0);
        img.setIsPrimary(isPrimary != null ? isPrimary : false);

        return ProductImageResponse.from(imageRepository.save(img));
    }

    // Sil
    @DeleteMapping("/{imageId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long productId, @PathVariable Long imageId) {
        ensureProductExists(productId);

        ProductImage img = imageRepository.findById(imageId)
                .orElseThrow(() -> new NotFoundException("Image not found: " + imageId));

        if (!img.getProduct().getId().equals(productId)) {
            throw new NotFoundException("Image not found: " + imageId);
        }

        imageRepository.delete(img);
    }

    private void ensureProductExists(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new NotFoundException("Product not found: " + productId);
        }
    }
}