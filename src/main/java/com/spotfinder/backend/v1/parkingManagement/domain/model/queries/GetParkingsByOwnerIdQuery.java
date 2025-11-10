package com.spotfinder.backend.v1.parkingManagement.domain.model.queries;

public record GetParkingsByOwnerIdQuery(Long ownerId) {
    public GetParkingsByOwnerIdQuery {
        if (ownerId == null) {
            throw new IllegalArgumentException("Owner ID cannot be null");
        }
    }
}
