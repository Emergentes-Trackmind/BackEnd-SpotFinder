package com.spotfinder.backend.v1.analytics.domain.model;

public record TopParkingDTO(
        String id,
        String name,
        int occupancyPercentage,
        double rating,
        double monthlyRevenue,
        String currency,
        String address,
        String status // 'active' | 'maintenance' | 'inactive'
) {}

