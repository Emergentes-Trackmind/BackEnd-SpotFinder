package com.spotfinder.backend.v1.reviews.domain.model.commands;

public record CreateReviewCommand(
        Long driverId,
        Long parkingId,
        String comment,
        Float rating
) {
    public CreateReviewCommand {
        if (driverId == null || driverId <= 0) {
            throw new IllegalArgumentException("Driver ID must be a positive number");
        }
        if (parkingId == null || parkingId <= 0) {
            throw new IllegalArgumentException("Parking ID must be a positive number");
        }
        if (comment == null || comment.isBlank()) {
            throw new IllegalArgumentException("Comment cannot be null or empty");
        }
        if (rating == null || rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
    }
}
