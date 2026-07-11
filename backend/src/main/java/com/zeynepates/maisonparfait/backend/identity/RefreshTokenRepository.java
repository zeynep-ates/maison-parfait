package com.zeynepates.maisonparfait.backend.identity;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    List<RefreshToken> findAllByUser_IdAndRevokedAtIsNullAndExpiresAtAfter(Long userId, OffsetDateTime now);

    // Ownership scoped at the query itself - a session id belonging to
    // another user simply isn't found, rather than needing a separate
    // ownership check (see docs/identity-module-design.md #12).
    Optional<RefreshToken> findByIdAndUser_Id(Long id, Long userId);
}
