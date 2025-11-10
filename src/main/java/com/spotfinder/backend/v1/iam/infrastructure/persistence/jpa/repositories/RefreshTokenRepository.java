package com.spotfinder.backend.v1.iam.infrastructure.persistence.jpa.repositories;

import com.spotfinder.backend.v1.iam.domain.model.entities.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {
    Optional<RefreshToken> findByToken(String token);

    // Derived delete query requires a transactional context
    @Modifying(clearAutomatically = true)
    @Transactional
    void deleteByUserId(Long userId);
}
