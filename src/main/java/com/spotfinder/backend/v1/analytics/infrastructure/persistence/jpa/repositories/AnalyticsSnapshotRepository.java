package com.spotfinder.backend.v1.analytics.infrastructure.persistence.jpa.repositories;

import com.spotfinder.backend.v1.analytics.domain.model.aggregates.AnalyticsSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnalyticsSnapshotRepository extends JpaRepository<AnalyticsSnapshot, Long> {
}

