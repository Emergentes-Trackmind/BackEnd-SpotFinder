package com.spotfinder.backend.v1.reservations.interfaces.rest.transform;

import com.spotfinder.backend.v1.reservations.domain.model.commands.UpdateReservationStatusCommand;

public class UpdateReservationCommandFromResourceAssembler {
    public static UpdateReservationStatusCommand toCommandFromResource(Long reservationId, String status) {
        String normalized = status != null ? status.trim().toUpperCase() : "";
        if ("CANCELLED".equals(normalized)) normalized = "CANCELED"; // frontend usa CANCELLED
        if ("PAID".equals(normalized)) normalized = "CONFIRMED"; // map to existing state
        return new UpdateReservationStatusCommand(reservationId, normalized);
    }
}
