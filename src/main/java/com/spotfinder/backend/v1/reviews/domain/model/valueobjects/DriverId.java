package com.spotfinder.backend.v1.reviews.domain.model.valueobjects;

import jakarta.persistence.Embeddable;

@Embeddable
public record DriverId(Long driverId) {
    public DriverId {
        if (driverId == null || driverId <= 0) {
            throw new IllegalArgumentException("Driver ID must be a positive number");
        }
    }

    public DriverId() {
        this(0L);
    }
}
