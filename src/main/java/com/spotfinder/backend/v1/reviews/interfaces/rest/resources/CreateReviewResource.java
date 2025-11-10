package com.spotfinder.backend.v1.reviews.interfaces.rest.resources;

public record CreateReviewResource(
        Long driverId,
        Long parkingId,
        String comment,
        Float rating
) {
}
