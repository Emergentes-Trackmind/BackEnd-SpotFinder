package com.spotfinder.backend.v1.parkingManagement.domain.model.queries;

import java.util.UUID;

public record GetParkingSpotByIdQuery(UUID parkingSpotId, Long parkingId) {
    public GetParkingSpotByIdQuery {
        if (parkingSpotId == null) {
            throw new IllegalArgumentException("Parking spot ID cannot be null");
        }
    }
}
