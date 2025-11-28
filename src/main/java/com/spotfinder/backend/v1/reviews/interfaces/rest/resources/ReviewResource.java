package com.spotfinder.backend.v1.reviews.interfaces.rest.resources;

public record ReviewResource(
        Long id,
        Long driverId,
        String driverName,
        String userEmail,
        String userAvatar,
        Long parkingId,
        String parkingName,
        Long parkingOwnerId,
        String comment,
        Float rating,
        String createdAt,
        Boolean responded,
        String responseText,
        String responseAt,
        String readAt,
        Boolean archived,
        String archivedAt
) {
}
