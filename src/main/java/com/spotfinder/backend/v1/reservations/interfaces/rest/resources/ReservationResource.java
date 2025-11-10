package com.spotfinder.backend.v1.reservations.interfaces.rest.resources;

public record ReservationResource(
        Long id,
        String driverFullName,
        Long driverId,
        String vehiclePlate,
        Long parkingId,
        String parkingSpotId,
        String spotLabel,
        String date,
        String startTime,
        String endTime,
        Float totalPrice,
        String status
) {
}
