package com.zeynepates.maisonparfait.backend.modules.admin;

import com.zeynepates.maisonparfait.backend.modules.tag.TagType;
import com.zeynepates.maisonparfait.backend.modules.tag.TagResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/tags")
public class AdminTagController {

    private final TagAdminService tagAdminService;

    @GetMapping
    public List<TagResponse> list(
            @RequestParam(defaultValue = "COLLECTION") TagType type,
            @RequestParam(defaultValue = "true") Boolean activeOnly
    ) {
        return tagAdminService.list(type, activeOnly);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TagResponse create(@Valid @RequestBody AdminTagRequest req) {
        return tagAdminService.create(req);
    }

    @PutMapping("/{id}")
    public TagResponse update(@PathVariable Long id, @Valid @RequestBody AdminTagRequest req) {
        return tagAdminService.update(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        tagAdminService.delete(id);
    }
}