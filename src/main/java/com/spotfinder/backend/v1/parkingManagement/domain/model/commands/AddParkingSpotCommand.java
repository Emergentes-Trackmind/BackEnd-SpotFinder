package com.spotfinder.backend.v1.parkingManagement.domain.model.commands;

public record AddParkingSpotCommand(
        Integer row,
        Integer column,
        String label,
        Long parkingId
) {
}
