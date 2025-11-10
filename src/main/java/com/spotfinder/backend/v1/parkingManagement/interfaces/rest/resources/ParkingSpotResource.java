package com.spotfinder.backend.v1.parkingManagement.interfaces.rest.resources;

import java.util.UUID;

public record ParkingSpotResource(
        UUID id,
        Long parkingId,
        Integer rowIndex,
        Integer columnIndex,
        String label,
        String status
) {
}