package com.spotfinder.backend.v1.analytics.domain.services;

import com.spotfinder.backend.v1.analytics.domain.model.*;
import com.spotfinder.backend.v1.analytics.domain.model.queries.*;

import java.util.List;

/**
 * Puerto de dominio para consultas de Analytics (KPIs y métricas del dashboard).
 */
public interface AnalyticsQueryService {
    // Métodos directos (compatibilidad)
    TotalsKpiDTO getTotals();
    TotalsKpiDTO getTotals(Long parkingId);
    List<RevenueByMonthDTO> getRevenue();
    List<OccupancyByHourDTO> getOccupancy();
    List<OccupancyByHourDTO> getOccupancy(Long parkingId);
    List<ActivityItemDTO> getActivity();
    List<ActivityItemDTO> getActivity(Long parkingId);
    List<TopParkingDTO> getTopParkings();

    // Manejo por queries (patrón simétrico a otros BC)
    TotalsKpiDTO handle(GetTotalsKpiQuery query);
    List<RevenueByMonthDTO> handle(GetRevenueByMonthQuery query);
    List<OccupancyByHourDTO> handle(GetOccupancyByHourQuery query);
    List<ActivityItemDTO> handle(GetActivityQuery query);
    List<TopParkingDTO> handle(GetTopParkingsQuery query);
}
