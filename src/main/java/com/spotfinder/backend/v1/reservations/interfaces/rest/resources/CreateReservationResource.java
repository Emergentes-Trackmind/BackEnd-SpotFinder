package com.spotfinder.backend.v1.reservations.interfaces.rest.resources;

import java.time.LocalDate;
import java.util.UUID;

public record CreateReservationResource(
        Long driverId,
        String vehiclePlate,
        Long parkingId,
        UUID parkingSpotId,
        LocalDate date,
        String startTime,
        String endTime
) {
}
