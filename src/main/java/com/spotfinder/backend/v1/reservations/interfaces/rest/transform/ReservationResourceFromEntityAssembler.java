package com.spotfinder.backend.v1.reservations.interfaces.rest.transform;

import com.spotfinder.backend.v1.parkingManagement.domain.model.aggregates.Parking;
import com.spotfinder.backend.v1.reservations.domain.model.aggregates.Reservation;
import com.spotfinder.backend.v1.reservations.interfaces.rest.resources.ReservationResource;

import java.time.LocalDateTime;
import java.util.Date;

public class ReservationResourceFromEntityAssembler {
    public static ReservationResource toResourceFromEntity(Reservation entity) {
        return toResourceFromEntity(entity, null);
    }

    public static ReservationResource toResourceFromEntity(Reservation entity, Parking parking) {
        String spotLabel = entity.getSpotLabel();
        String startIso = null;
        String endIso = null;

        if (entity.getDate() != null && entity.getStartTime() != null) {
            startIso = LocalDateTime.of(entity.getDate(), entity.getStartTime()).toString();
        } else if (entity.getStartTime() != null) {
            startIso = entity.getStartTime().toString();
        }

        if (entity.getDate() != null && entity.getEndTime() != null) {
            endIso = LocalDateTime.of(entity.getDate(), entity.getEndTime()).toString();
        } else if (entity.getEndTime() != null) {
            endIso = entity.getEndTime().toString();
        }

        String createdAt = toIsoString(entity.getCreatedAt());
        String updatedAt = toIsoString(entity.getUpdatedAt());
        String parkingName = parking != null ? parking.getName() : null;
        Long parkingOwnerId = parking != null ? parking.getOwnerId() : null;

        return new ReservationResource(
                entity.getId(),
                entity.getDriverId(), // userId (alias for driver)
                entity.getDriverId(),
                entity.getDriverName(),
                entity.getDriverName(),
                null, // email not stored yet
                entity.getVehiclePlate(),
                entity.getParkingId(),
                parkingName,
                parkingOwnerId,
                entity.getParkingSpotId(),
                spotLabel,
                spotLabel,
                entity.getDate() != null ? entity.getDate().toString() : null,
                startIso,
                endIso,
                entity.getTotalPrice(),
                "USD",
                entity.getStatus(),
                createdAt,
                updatedAt
        );
    }

    private static String toIsoString(Date date) {
        return date != null ? date.toInstant().toString() : null;
    }
}
