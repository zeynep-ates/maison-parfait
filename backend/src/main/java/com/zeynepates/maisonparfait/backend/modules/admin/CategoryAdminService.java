package com.zeynepates.maisonparfait.backend.modules.admin;

import com.zeynepates.maisonparfait.backend.common.exception.ConflictException;
import com.zeynepates.maisonparfait.backend.common.exception.NotFoundException;
import com.zeynepates.maisonparfait.backend.modules.category.Category;
import com.zeynepates.maisonparfait.backend.modules.category.CategoryKind;
import com.zeynepates.maisonparfait.backend.modules.category.CategoryRepository;
import com.zeynepates.maisonparfait.backend.modules.category.CategoryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryAdminService {

    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public List<CategoryResponse> list(CategoryKind kind, Boolean activeOnly) {
        if (Boolean.TRUE.equals(activeOnly)) {
            return categoryRepository.findAllByKindAndIsActiveTrueOrderBySortOrderAscIdAsc(kind)
                    .stream().map(CategoryResponse::from).toList();
        }
        return categoryRepository.findAll().stream()
                .filter(c -> c.getKind() == kind)
                .sorted((a, b) -> {
                    int cmp = Integer.compare(a.getSortOrder(), b.getSortOrder());
                    if (cmp != 0) return cmp;
                    return Long.compare(a.getId(), b.getId());
                })
                .map(CategoryResponse::from).toList();
    }

    @Transactional
    public CategoryResponse create(AdminCategoryRequest req) {
        if (categoryRepository.existsBySlug(req.slug())) {
            throw new ConflictException("Category slug already exists: " + req.slug());
        }

        Category c = new Category();
        apply(c, req);

        return CategoryResponse.from(categoryRepository.save(c));
    }

    @Transactional
    public CategoryResponse update(Long id, AdminCategoryRequest req) {
        Category c = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category not found: " + id));

        if (!c.getSlug().equals(req.slug()) && categoryRepository.existsBySlug(req.slug())) {
            throw new ConflictException("Category slug already exists: " + req.slug());
        }

        apply(c, req);
        return CategoryResponse.from(c);
    }

    @Transactional
    public void delete(Long id) {
        Category c = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category not found: " + id));
        c.setIsActive(false);
    }

    private void apply(Category c, AdminCategoryRequest req) {
        c.setName(req.name());
        c.setSlug(req.slug());
        c.setKind(req.kind());
        if (req.sortOrder() != null) c.setSortOrder(req.sortOrder());
        if (req.isActive() != null) c.setIsActive(req.isActive());

        if (req.parentId() != null) {
            Category parent = categoryRepository.findById(req.parentId())
                    .orElseThrow(() -> new NotFoundException("Parent category not found: " + req.parentId()));
            c.setParent(parent);
        } else {
            c.setParent(null);
        }
    }
}
