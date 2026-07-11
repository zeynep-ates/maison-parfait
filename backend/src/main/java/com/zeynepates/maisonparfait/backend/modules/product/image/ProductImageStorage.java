package com.zeynepates.maisonparfait.backend.modules.product.image;

import com.zeynepates.maisonparfait.backend.common.exception.ConflictException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.*;
import java.util.Set;
import java.util.UUID;

@Service
public class ProductImageStorage {

    private static final Set<String> ALLOWED_EXT = Set.of("jpg", "jpeg", "png", "webp");

    @Value("${app.uploads.dir:uploads}")
    private String uploadsDir;

    public String saveProductImage(Long productId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ConflictException("File is empty");
        }

        String original = file.getOriginalFilename() == null ? "image" : file.getOriginalFilename();
        String ext = detectExt(original);
        String filename = UUID.randomUUID() + "." + ext;

        Path dir = Paths.get(uploadsDir, "products", String.valueOf(productId));
        Path target = dir.resolve(filename);

        try {
            Files.createDirectories(dir);
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Exception e) {
            throw new ConflictException("Upload failed: " + e.getMessage());
        }

        return "/uploads/products/" + productId + "/" + filename;
    }

    private String detectExt(String filename) {
        String lower = filename.toLowerCase();
        int dot = lower.lastIndexOf('.');
        String ext = dot >= 0 ? lower.substring(dot + 1) : "";
        if (!ALLOWED_EXT.contains(ext)) {
            throw new ConflictException("Unsupported file type. Allowed: jpg, jpeg, png, webp");
        }
        return ext.equals("jpeg") ? "jpg" : ext;
    }
}
