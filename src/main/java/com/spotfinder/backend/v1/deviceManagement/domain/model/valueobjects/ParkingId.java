package com.spotfinder.backend.v1.deviceManagement.domain.model.valueobjects;

import jakarta.persistence.Embeddable;

@Embeddable
public record ParkingId(Long parkingId) {
    public ParkingId {
        if (parkingId == null) {
            throw new IllegalArgumentException("Parking ID cannot be null");
        }
    }
    public ParkingId() {
        this(0L);
    }
}
