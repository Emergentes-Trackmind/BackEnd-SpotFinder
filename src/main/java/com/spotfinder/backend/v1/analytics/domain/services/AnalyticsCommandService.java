package com.spotfinder.backend.v1.analytics.domain.services;

import com.spotfinder.backend.v1.analytics.domain.model.TotalsKpiDTO;
import com.spotfinder.backend.v1.analytics.domain.model.aggregates.AnalyticsSnapshot;

public interface AnalyticsCommandService {
    AnalyticsSnapshot createSnapshot(TotalsKpiDTO dto);
}

