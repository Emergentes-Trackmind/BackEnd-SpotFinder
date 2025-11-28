package com.spotfinder.backend.v1.reservations.interfaces.rest.resources;

public record ReservationResource(
        Long id,
        Long userId,
        Long driverId,
        String userName,
        String driverFullName,
        String userEmail,
        String vehiclePlate,
        Long parkingId,
        String parkingName,
        Long parkingOwnerId,
        String parkingSpotId,
        String spotLabel,
        String space,
        String date,
        String startTime,
        String endTime,
        Float totalPrice,
        String currency,
        String status,
        String createdAt,
        String updatedAt
) {
}
