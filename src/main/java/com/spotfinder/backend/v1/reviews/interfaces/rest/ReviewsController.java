package com.spotfinder.backend.v1.reviews.interfaces.rest;

import com.spotfinder.backend.v1.reviews.domain.model.aggregates.Review;
import com.spotfinder.backend.v1.reviews.domain.model.queries.GetReviewsByDriverIdQuery;
import com.spotfinder.backend.v1.reviews.domain.model.queries.GetReviewsByParkingIdQuery;
import com.spotfinder.backend.v1.reviews.domain.services.ReviewCommandService;
import com.spotfinder.backend.v1.reviews.domain.services.ReviewQueryService;
import com.spotfinder.backend.v1.reviews.interfaces.rest.resources.CreateReviewResource;
import com.spotfinder.backend.v1.reviews.interfaces.rest.resources.ReviewResource;
import com.spotfinder.backend.v1.reviews.interfaces.rest.transform.CreateReviewCommandFromResourceAssembler;
import com.spotfinder.backend.v1.reviews.interfaces.rest.transform.ReviewResourceFromEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value = {"/api/v1/reviews", "/api/reviews"}, produces = APPLICATION_JSON_VALUE)
@Tag(name = "Review", description = "Review management")
public class ReviewsController {
    private final ReviewCommandService reviewCommandService;
    private final ReviewQueryService reviewQueryService;

    public ReviewsController(ReviewCommandService reviewCommandService, ReviewQueryService reviewQueryService) {
        this.reviewCommandService = reviewCommandService;
        this.reviewQueryService = reviewQueryService;
    }

    @Operation(summary = "Create a new review")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Review created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    @PostMapping
    public ResponseEntity<ReviewResource> createReview(@RequestBody CreateReviewResource resource) {
        Optional<Review> review = this.reviewCommandService
                .handle(CreateReviewCommandFromResourceAssembler.toCommandFromResource(resource));

        return review.map(source ->
                        new ResponseEntity<>(ReviewResourceFromEntityAssembler.toResourceFromEntity(source), CREATED))
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    @Operation(summary = "Get all reviews by parking id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reviews retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "No reviews found")
    })
    @GetMapping("/parking/{parkingId}")
    public ResponseEntity<List<ReviewResource>> getReviewsByParkingId(@PathVariable Long parkingId) {
        var query = new GetReviewsByParkingIdQuery(parkingId);
        List<Review> reviews = this.reviewQueryService.handle(query);

        if (reviews.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<ReviewResource> resources = reviews.stream()
                .map(ReviewResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(resources);
    }

    @Operation(summary = "Get all reviews by driver id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reviews retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "No reviews found")
    })
    @GetMapping("/driver/{driverId}")
    public ResponseEntity<List<ReviewResource>> getReviewsByDriverId(@PathVariable Long driverId) {
        var query = new GetReviewsByDriverIdQuery(driverId);
        List<Review> reviews = this.reviewQueryService.handle(query);

        if (reviews.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<ReviewResource> resources = reviews.stream()
                .map(ReviewResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(resources);
    }
}
