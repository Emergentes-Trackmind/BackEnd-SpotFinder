package com.spotfinder.backend.v1.analytics.interfaces.rest.resources;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TotalsKpiResource(
    @JsonProperty("totalRevenue") RevenueKpi revenue,
    @JsonProperty("occupiedSpaces") OccupancyKpi occupancy,
    @JsonProperty("activeUsers") UsersKpi users,
    @JsonProperty("registeredParkings") ParkingsKpi parkings
) {
    public record RevenueKpi(
        double value,
        String currency,
        double deltaPercentage,
        String deltaText,
        String text
    ) {}

    public record OccupancyKpi(
        int occupied,
        int total,
        int percentage,
        String text,
        String deltaText
    ) {}

    public record UsersKpi(
        @JsonProperty("count") int total,
        double deltaPercentage,
        @JsonProperty("deltaText") String text,
        @JsonProperty("newThisMonth") int newUsers
    ) {}

    public record ParkingsKpi(
        int total,
        @JsonProperty("newThisMonth") int newCount,
        @JsonProperty("deltaText") String text,
        double deltaPercentage
    ) {}
}
