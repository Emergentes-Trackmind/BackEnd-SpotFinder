package com.spotfinder.backend.v1.notifications.domain.repository;

import com.spotfinder.backend.v1.notifications.domain.model.FcmToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FcmTokenRepository extends JpaRepository<FcmToken, Long> {
    boolean existsByToken(String token);
    void deleteByToken(String token);
    Optional<FcmToken> findByUserId(Long userId);
    List<FcmToken> findAllByUserId(Long userId);
    Optional<FcmToken> findByToken(String token);

}
