package com.spotfinder.backend.v1.reservations.domain.model.queries;

public record GetAllReservationsByDriverIdAndStatusQuery(Long driverId, String status) {
    public GetAllReservationsByDriverIdAndStatusQuery {
        if (driverId == null || status == null || status.isBlank()) {
            throw new IllegalArgumentException("Driver ID and status must not be null or empty");
        }
        if (driverId <= 0) {
            throw new IllegalArgumentException("Driver ID must be a positive number");
        }
    }
}
