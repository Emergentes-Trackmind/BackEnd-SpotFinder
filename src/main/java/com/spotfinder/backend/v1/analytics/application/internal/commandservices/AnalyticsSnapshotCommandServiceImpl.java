package com.spotfinder.backend.v1.analytics.application.internal.commandservices;

import com.spotfinder.backend.v1.analytics.domain.model.TotalsKpiDTO;
import com.spotfinder.backend.v1.analytics.domain.model.aggregates.AnalyticsSnapshot;
import com.spotfinder.backend.v1.analytics.domain.services.AnalyticsCommandService;
import com.spotfinder.backend.v1.analytics.infrastructure.persistence.jpa.repositories.AnalyticsSnapshotRepository;
import org.springframework.stereotype.Service;

@Service
public class AnalyticsSnapshotCommandServiceImpl implements AnalyticsCommandService {

    private final AnalyticsSnapshotRepository repository;

    public AnalyticsSnapshotCommandServiceImpl(AnalyticsSnapshotRepository repository) {
        this.repository = repository;
    }

    @Override
    public AnalyticsSnapshot createSnapshot(TotalsKpiDTO dto) {
        var snapshot = new AnalyticsSnapshot(dto);
        return repository.save(snapshot);
    }
}

