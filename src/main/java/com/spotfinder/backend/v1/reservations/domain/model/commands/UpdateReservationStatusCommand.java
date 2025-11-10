package com.spotfinder.backend.v1.reservations.domain.model.commands;

public record UpdateReservationStatusCommand(
        Long reservationId,
        String status
) {
}
