package com.spotfinder.backend.v1.reviews.domain.model.queries;

public record GetReviewsByParkingIdQuery(Long parkingId) {
    public GetReviewsByParkingIdQuery {
        if (parkingId == null || parkingId <= 0) {
            throw new IllegalArgumentException("Parking ID must be a positive number.");
        }
    }
}
