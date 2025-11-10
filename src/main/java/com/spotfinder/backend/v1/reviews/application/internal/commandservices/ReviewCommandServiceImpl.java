package com.spotfinder.backend.v1.reviews.application.internal.commandservices;

import com.spotfinder.backend.v1.reviews.application.internal.outboundservices.acl.ExternalParkingService;
import com.spotfinder.backend.v1.reviews.application.internal.outboundservices.acl.ExternalProfileService;
import com.spotfinder.backend.v1.reviews.domain.model.aggregates.Review;
import com.spotfinder.backend.v1.reviews.domain.model.commands.CreateReviewCommand;
import com.spotfinder.backend.v1.reviews.domain.services.ReviewCommandService;
import com.spotfinder.backend.v1.reviews.infrastructure.persistence.jpa.repositories.ReviewRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ReviewCommandServiceImpl implements ReviewCommandService {
    private final ReviewRepository reviewRepository;
    private final ExternalParkingService externalParkingService;
    private final ExternalProfileService externalProfileService;

    public ReviewCommandServiceImpl(ReviewRepository reviewRepository, ExternalParkingService externalParkingService, ExternalProfileService externalProfileService) {
        this.reviewRepository = reviewRepository;
        this.externalParkingService = externalParkingService;
        this.externalProfileService = externalProfileService;
    }

    @Override
    public Optional<Review> handle(CreateReviewCommand command) {
        var driverName = externalProfileService.getDriverFullNameByUserId(command.driverId());
        if (driverName == null) {
            return Optional.empty();
        }
        var parkingName = externalParkingService.getParkingName(command.parkingId());
        if (parkingName == null) {
            return Optional.empty();
        }
        var review = new Review(command, driverName, parkingName);
        var savedReview = reviewRepository.save(review);
        if (savedReview.getId() != null) {
            externalParkingService.updateParkingRating(command.parkingId(), command.rating());

            return Optional.of(savedReview);
        }
        return Optional.empty();
    }
}
