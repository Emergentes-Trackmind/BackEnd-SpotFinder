package com.spotfinder.backend.v1.parkingManagement.domain.model.commands;

public record CreateParkingCommand(
        Long ownerId,
        String name,
        String description,
        String type,
        String phone,
        String email,
        String website,
        String status,
        String address,
        Double lat,
        Double lng,
        Float ratePerHour,
        Integer totalSpots,
        Integer availableSpots,
        Integer totalRows,
        Integer totalColumns,
        String imageUrl
) {
}
