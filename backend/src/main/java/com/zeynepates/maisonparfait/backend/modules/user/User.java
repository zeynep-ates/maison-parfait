package com.zeynepates.maisonparfait.backend.modules.user;

import com.zeynepates.maisonparfait.backend.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

/**
 * Temporarily stays in modules.user - see docs/identity-module-design.md.
 * The identity module reuses this entity as-is rather than mapping a second
 * class onto the same "users" table; this class moves to the identity
 * package (and other modules' imports get updated) in Phase 1F.
 *
 * email uniqueness is enforced by a partial unique index in V8 (excludes
 * soft-deleted rows), not by @Column(unique = true) - ddl-auto is
 * "validate", so that annotation would have no schema effect and would
 * misdescribe the real constraint if left on.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User extends BaseEntity {

    @Column(name = "email", length = 255, nullable = false)
    private String email;

    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    @Column(name = "full_name", length = 150)
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 50, nullable = false)
    private UserRole role = UserRole.CUSTOMER;

    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;

    @Column(name = "locked_at")
    private OffsetDateTime lockedAt;

    @Column(name = "email_verified_at")
    private OffsetDateTime emailVerifiedAt;

    @Column(name = "pending_email", length = 255)
    private String pendingEmail;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;
}
