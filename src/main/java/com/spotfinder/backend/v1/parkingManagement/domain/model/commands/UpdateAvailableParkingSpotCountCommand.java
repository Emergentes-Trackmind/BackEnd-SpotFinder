package com.spotfinder.backend.v1.parkingManagement.domain.model.commands;

public record UpdateAvailableParkingSpotCountCommand(Long parkingId, Integer numberAvailable, String operation) {
    public UpdateAvailableParkingSpotCountCommand {
        if (parkingId == null || parkingId <= 0) {
            throw new IllegalArgumentException("Parking ID must be a positive number");
        }
        if (numberAvailable == null || numberAvailable < 0) {
            throw new IllegalArgumentException("Number of available spots must be a non-negative number");
        }
        if (operation == null || operation.isEmpty()) {
            throw new IllegalArgumentException("Operation must not be null or empty");
        }
    }
}
