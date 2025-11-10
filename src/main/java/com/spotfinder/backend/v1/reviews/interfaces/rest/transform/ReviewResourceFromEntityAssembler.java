package com.spotfinder.backend.v1.reviews.interfaces.rest.transform;

import com.spotfinder.backend.v1.reviews.domain.model.aggregates.Review;
import com.spotfinder.backend.v1.reviews.interfaces.rest.resources.ReviewResource;

public class ReviewResourceFromEntityAssembler {
    public static ReviewResource toResourceFromEntity(Review entity) {
        return new ReviewResource(
                entity.getId(),
                entity.getDriverId(),
                entity.getDriverName(),
                entity.getParkingId(),
                entity.getParkingName(),
                entity.getComment(),
                entity.getRating(),
                entity.getCreatedAt().toString()
        );
    }
}
