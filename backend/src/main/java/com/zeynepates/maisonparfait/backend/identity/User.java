package com.zeynepates.maisonparfait.backend.identity;

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
 * Maps to the "users" table. email uniqueness is enforced by a partial
 * unique index in V8 (excludes soft-deleted rows), not by
 * @Column(unique = true) - ddl-auto is "validate", so that annotation
 * would have no schema effect and would misdescribe the real constraint
 * if left on.
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
