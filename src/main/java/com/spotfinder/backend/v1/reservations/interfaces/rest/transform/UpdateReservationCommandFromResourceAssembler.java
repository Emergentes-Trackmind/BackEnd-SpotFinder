package com.spotfinder.backend.v1.reservations.interfaces.rest.transform;

import com.spotfinder.backend.v1.reservations.domain.model.commands.UpdateReservationStatusCommand;

public class UpdateReservationCommandFromResourceAssembler {
    public static UpdateReservationStatusCommand toCommandFromResource(Long reservationId, String status) {
        return new UpdateReservationStatusCommand(reservationId, status);
    }
}
