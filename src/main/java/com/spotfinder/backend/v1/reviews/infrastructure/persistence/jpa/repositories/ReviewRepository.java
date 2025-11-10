package com.spotfinder.backend.v1.reviews.infrastructure.persistence.jpa.repositories;

import com.spotfinder.backend.v1.reviews.domain.model.aggregates.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findAllByDriverIdDriverId(Long driverId);
    List<Review> findAllByParkingIdParkingId(Long parkingId);
}
