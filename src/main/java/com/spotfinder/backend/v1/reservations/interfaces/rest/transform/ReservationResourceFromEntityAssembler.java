package com.spotfinder.backend.v1.reservations.interfaces.rest.transform;

import com.spotfinder.backend.v1.reservations.domain.model.aggregates.Reservation;
import com.spotfinder.backend.v1.reservations.interfaces.rest.resources.ReservationResource;

public class ReservationResourceFromEntityAssembler {
    public static ReservationResource toResourceFromEntity(Reservation entity) {
        return new ReservationResource(
                entity.getId(),
                entity.getDriverName(),
                entity.getDriverId(),
                entity.getVehiclePlate(),
                entity.getParkingId(),
                entity.getParkingSpotId(),
                entity.getSpotLabel(),
                entity.getDate().toString(),
                entity.getStartTime().toString(),
                entity.getEndTime().toString(),
                entity.getTotalPrice(),
                entity.getStatus()
        );
    }
}
