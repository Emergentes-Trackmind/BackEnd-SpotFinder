package com.spotfinder.backend.v1.analytics.interfaces.acl;

import com.spotfinder.backend.v1.analytics.domain.model.*;

import java.util.List;

/**
 * Public Facade for other bounded contexts to query Analytics data
 * without acoplarse a detalles internos del módulo.
 */
public interface AnalyticsContextFacade {
    TotalsKpiDTO getTotals();
    List<RevenueByMonthDTO> getRevenue();
    List<OccupancyByHourDTO> getOccupancy();
    List<ActivityItemDTO> getActivity();
    List<TopParkingDTO> getTopParkings();
}

