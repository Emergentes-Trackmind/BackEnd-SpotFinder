package com.spotfinder.backend.v1.analytics.domain.model;

public record TotalsKpiDTO(
        RevenueKpi revenue,
        OccupiedSpacesKpi occupancy,
        ActiveUsersKpi users,
        RegisteredParkingsKpi parkings
) {
    public record RevenueKpi(
        double value,
        String currency,
        double deltaPercentage,
        String deltaText,
        String text
    ) {}

    public record OccupiedSpacesKpi(
        int occupied,
        int total,
        int percentage,
        String text,
        String deltaText
    ) {}

    public record ActiveUsersKpi(
        int total,
        double deltaPercentage,
        String text,
        int newUsers
    ) {}

    public record RegisteredParkingsKpi(
        int total,
        int newCount,
        String text,
        double deltaPercentage
    ) {}
}

