package com.spotfinder.backend.v1.analytics.application.internal.outboundservices.acl;

import com.spotfinder.backend.v1.iam.domain.model.aggregates.User;
import com.spotfinder.backend.v1.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Analytics ACL facade to get users metadata for KPIs.
 * Renamed to avoid bean name collision with profile module.
 */
@Component
public class AnalyticsExternalUserService {
    private final UserRepository userRepository;

    public AnalyticsExternalUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> findAll() { return userRepository.findAll(); }
}
