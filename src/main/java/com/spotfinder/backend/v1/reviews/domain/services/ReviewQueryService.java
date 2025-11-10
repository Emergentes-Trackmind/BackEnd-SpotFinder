package com.spotfinder.backend.v1.reviews.domain.services;

import com.spotfinder.backend.v1.reviews.domain.model.aggregates.Review;
import com.spotfinder.backend.v1.reviews.domain.model.queries.GetReviewsByDriverIdQuery;
import com.spotfinder.backend.v1.reviews.domain.model.queries.GetReviewsByParkingIdQuery;

import java.util.List;

public interface ReviewQueryService {
    List<Review> handle(GetReviewsByDriverIdQuery query);
    List<Review> handle(GetReviewsByParkingIdQuery query);
}
