package com.zeynepates.maisonparfait.backend.modules.admin;

import com.zeynepates.maisonparfait.backend.common.exception.ConflictException;
import com.zeynepates.maisonparfait.backend.common.exception.NotFoundException;
import com.zeynepates.maisonparfait.backend.modules.tag.Tag;
import com.zeynepates.maisonparfait.backend.modules.tag.TagRepository;
import com.zeynepates.maisonparfait.backend.modules.tag.TagResponse;
import com.zeynepates.maisonparfait.backend.modules.tag.TagType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TagAdminService {

    private final TagRepository tagRepository;

    @Transactional(readOnly = true)
    public List<TagResponse> list(TagType type, Boolean activeOnly) {
        if (Boolean.TRUE.equals(activeOnly)) {
            return tagRepository.findAllByTypeAndIsActiveTrueOrderBySortOrderAscIdAsc(type)
                    .stream().map(TagResponse::from).toList();
        }

        return tagRepository.findAll().stream()
                .filter(t -> t.getType() == type)
                .sorted(Comparator
                        .comparing(Tag::getSortOrder)
                        .thenComparing(Tag::getId))
                .map(TagResponse::from)
                .toList();
    }

    @Transactional
    public TagResponse create(AdminTagRequest req) {
        if (tagRepository.existsBySlug(req.slug())) {
            throw new ConflictException("Tag slug already exists: " + req.slug());
        }

        Tag t = new Tag();
        apply(t, req);

        return TagResponse.from(tagRepository.save(t));
    }

    @Transactional
    public TagResponse update(Long id, AdminTagRequest req) {
        Tag t = tagRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Tag not found: " + id));

        if (!t.getSlug().equals(req.slug()) && tagRepository.existsBySlug(req.slug())) {
            throw new ConflictException("Tag slug already exists: " + req.slug());
        }

        apply(t, req);
        return TagResponse.from(t);
    }

    @Transactional
    public void delete(Long id) {
        Tag t = tagRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Tag not found: " + id));
        t.setIsActive(false);
    }

    private void apply(Tag t, AdminTagRequest req) {
        t.setType(req.type());
        t.setName(req.name());
        t.setSlug(req.slug());
        if (req.sortOrder() != null) t.setSortOrder(req.sortOrder());
        if (req.isActive() != null) t.setIsActive(req.isActive());
    }
}