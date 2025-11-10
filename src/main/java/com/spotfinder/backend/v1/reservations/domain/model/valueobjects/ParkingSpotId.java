package com.spotfinder.backend.v1.reservations.domain.model.valueobjects;

import jakarta.persistence.Embeddable;

import java.util.UUID;

@Embeddable
public record ParkingSpotId(UUID parkingSpotId) {
    public ParkingSpotId {
        if (parkingSpotId == null || parkingSpotId.toString().isEmpty()) {
            throw new IllegalArgumentException("Parking Spot ID must be a valid UUID");
        }
    }
    public ParkingSpotId() {
        this(UUID.randomUUID());
    } // Default constructor for serialization/deserialization
}
