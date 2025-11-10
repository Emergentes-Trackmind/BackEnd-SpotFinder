package com.spotfinder.backend.v1.analytics.application.acl;

import com.spotfinder.backend.v1.analytics.domain.model.*;
import com.spotfinder.backend.v1.analytics.domain.services.AnalyticsQueryService;
import com.spotfinder.backend.v1.analytics.interfaces.acl.AnalyticsContextFacade;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AnalyticsContextFacadeImpl implements AnalyticsContextFacade {

    private final AnalyticsQueryService queryService;

    public AnalyticsContextFacadeImpl(AnalyticsQueryService queryService) {
        this.queryService = queryService;
    }

    @Override
    public TotalsKpiDTO getTotals() { return queryService.getTotals(); }

    @Override
    public List<RevenueByMonthDTO> getRevenue() { return queryService.getRevenue(); }

    @Override
    public List<OccupancyByHourDTO> getOccupancy() { return queryService.getOccupancy(); }

    @Override
    public List<ActivityItemDTO> getActivity() { return queryService.getActivity(); }

    @Override
    public List<TopParkingDTO> getTopParkings() { return queryService.getTopParkings(); }
}

