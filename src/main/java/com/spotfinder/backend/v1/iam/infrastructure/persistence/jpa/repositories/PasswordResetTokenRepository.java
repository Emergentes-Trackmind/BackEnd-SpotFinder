package com.spotfinder.backend.v1.iam.infrastructure.persistence.jpa.repositories;

import com.spotfinder.backend.v1.iam.domain.model.entities.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, String> {
    Optional<PasswordResetToken> findByToken(String token);
}

