package com.spotfinder.backend.v1.reviews.interfaces.rest.transform;

import com.spotfinder.backend.v1.reviews.domain.model.aggregates.Review;
import com.spotfinder.backend.v1.reviews.interfaces.rest.resources.ReviewResource;
import com.spotfinder.backend.v1.parkingManagement.domain.model.aggregates.Parking;

public class ReviewResourceFromEntityAssembler {
    public static ReviewResource toResourceFromEntity(Review entity, Parking parking) {
        Long ownerId = parking != null ? parking.getOwnerId() : null;
        return new ReviewResource(
                entity.getId(),
                entity.getDriverId(),
                entity.getDriverName(),
                entity.getUserEmail(),
                entity.getUserAvatar(),
                entity.getParkingId(),
                entity.getParkingName(),
                ownerId,
                entity.getComment(),
                entity.getRating(),
                entity.getCreatedAt() != null ? entity.getCreatedAt().toString() : null,
                entity.getResponded(),
                entity.getResponseText(),
                entity.getResponseAt() != null ? entity.getResponseAt().toString() : null,
                entity.getReadAt() != null ? entity.getReadAt().toString() : null,
                entity.getArchived(),
                entity.getArchivedAt() != null ? entity.getArchivedAt().toString() : null
        );
    }
}
