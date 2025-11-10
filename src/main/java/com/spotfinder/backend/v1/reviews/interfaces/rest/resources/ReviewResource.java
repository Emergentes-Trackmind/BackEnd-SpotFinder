package com.spotfinder.backend.v1.reviews.interfaces.rest.resources;

public record ReviewResource(
        Long id,
        Long driverId,
        String driverName,
        Long parkingId,
        String parkingName,
        String comment,
        Float rating,
        String createdAt
) {
}
