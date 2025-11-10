package com.spotfinder.backend.v1.analytics.interfaces.rest.resources;

public record TopParkingResource(
        String id,
        String name,
        int occupancyPercentage,
        double rating,
        double monthlyRevenue,
        String currency,
        String address,
        String status
) {}

