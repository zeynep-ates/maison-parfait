package com.zeynepates.maisonparfait.backend.modules.tag;

import com.zeynepates.maisonparfait.backend.modules.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "tags")
public class Tag extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 50, nullable = false)
    private TagType type;

    @Column(name = "name", length = 120, nullable = false)
    private String name;

    @Column(name = "slug", length = 140, nullable = false, unique = true)
    private String slug;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}
