package com.spotfinder.backend.v1.reviews.application.internal.queryservices;

import com.spotfinder.backend.v1.reviews.domain.model.aggregates.Review;
import com.spotfinder.backend.v1.reviews.domain.model.queries.GetReviewsByDriverIdQuery;
import com.spotfinder.backend.v1.reviews.domain.model.queries.GetReviewsByParkingIdQuery;
import com.spotfinder.backend.v1.reviews.domain.services.ReviewQueryService;
import com.spotfinder.backend.v1.reviews.infrastructure.persistence.jpa.repositories.ReviewRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReviewQueryServiceImpl implements ReviewQueryService {
    private final ReviewRepository reviewRepository;

    public ReviewQueryServiceImpl(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    @Override
    public List<Review> handle(GetReviewsByDriverIdQuery query) {
        return reviewRepository.findAllByDriverIdDriverId(query.driverId());
    }

    @Override
    public List<Review> handle(GetReviewsByParkingIdQuery query) {
        return reviewRepository.findAllByParkingIdParkingId(query.parkingId());
    }
}
