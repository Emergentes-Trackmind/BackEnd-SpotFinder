package com.spotfinder.backend.v1.reviews.domain.model.queries;

public record GetReviewsByDriverIdQuery(Long driverId) {
    public GetReviewsByDriverIdQuery {
        if (driverId == null || driverId <= 0) {
            throw new IllegalArgumentException("Driver ID must be a positive number.");
        }
    }
}
