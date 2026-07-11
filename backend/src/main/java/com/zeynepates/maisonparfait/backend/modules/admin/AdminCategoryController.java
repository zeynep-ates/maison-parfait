package com.zeynepates.maisonparfait.backend.modules.admin;

import com.zeynepates.maisonparfait.backend.modules.category.*;
import com.zeynepates.maisonparfait.backend.modules.category.CategoryResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/categories")
public class AdminCategoryController {

    private final CategoryAdminService categoryAdminService;

    @GetMapping
    public List<CategoryResponse> list(
            @RequestParam(defaultValue = "PRODUCT") CategoryKind kind,
            @RequestParam(defaultValue = "true") Boolean activeOnly
    ) {
        return categoryAdminService.list(kind, activeOnly);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponse create(@Valid @RequestBody AdminCategoryRequest req) {
        return categoryAdminService.create(req);
    }

    @PutMapping("/{id}")
    public CategoryResponse update(@PathVariable Long id, @Valid @RequestBody AdminCategoryRequest req) {
        return categoryAdminService.update(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        categoryAdminService.delete(id);
    }
}