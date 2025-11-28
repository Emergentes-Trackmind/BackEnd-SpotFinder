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
import com.spotfinder.backend.v1.reviews.infrastructure.persistence.jpa.repositories.ReviewRepository;
import com.spotfinder.backend.v1.parkingManagement.domain.model.aggregates.Parking;
import com.spotfinder.backend.v1.parkingManagement.domain.model.valueobjects.OwnerId;
import com.spotfinder.backend.v1.parkingManagement.infrastructure.persistence.jpa.repositories.ParkingRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.HashMap;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value = {"/api/v1/reviews", "/api/reviews"}, produces = APPLICATION_JSON_VALUE)
@Tag(name = "Review", description = "Review management")
public class ReviewsController {
    private final ReviewCommandService reviewCommandService;
    private final ReviewQueryService reviewQueryService;
    private final ParkingRepository parkingRepository;
    private final ReviewRepository reviewRepository;

    public ReviewsController(ReviewCommandService reviewCommandService, ReviewQueryService reviewQueryService,
                             ParkingRepository parkingRepository, ReviewRepository reviewRepository) {
        this.reviewCommandService = reviewCommandService;
        this.reviewQueryService = reviewQueryService;
        this.parkingRepository = parkingRepository;
        this.reviewRepository = reviewRepository;
    }

    @GetMapping
    @Operation(summary = "List reviews (owner view compatible with web frontend)")
    public ResponseEntity<List<ReviewResource>> listReviews(
            @RequestParam(required = false) String currentUserId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer rating,
            @RequestParam(required = false) Long parkingId,
            @RequestParam(required = false) String q,
            @RequestParam(name = "createdAt_gte", required = false) String createdGte,
            @RequestParam(name = "createdAt_lte", required = false) String createdLte,
            @RequestParam(name = "_page", defaultValue = "1") int page,
            @RequestParam(name = "_limit", defaultValue = "10") int limit,
            @RequestParam(name = "_sort", defaultValue = "createdAt") String sort,
            @RequestParam(name = "_order", defaultValue = "desc") String order
    ) {
        List<Review> all = reviewRepository.findAll();

        // Filtrar por owner
        if (currentUserId != null && !currentUserId.isBlank()) {
            try {
                Long ownerId = Long.parseLong(currentUserId);
                Set<Long> parkingIds = parkingRepository.findParkingsByOwnerId(new OwnerId(ownerId))
                        .stream().map(Parking::getId).collect(Collectors.toSet());
                all = all.stream().filter(r -> parkingIds.contains(r.getParkingId())).toList();
            } catch (NumberFormatException ignored) {}
        }

        if (parkingId != null) {
            all = all.stream().filter(r -> r.getParkingId().equals(parkingId)).toList();
        }
        if (rating != null) {
            all = all.stream().filter(r -> Math.round(r.getRating()) == rating).toList();
        }
        if (status != null && !status.isBlank()) {
            String st = status.toLowerCase();
            all = all.stream().filter(r -> {
                boolean responded = Boolean.TRUE.equals(r.getResponded());
                boolean archived = Boolean.TRUE.equals(r.getArchived());
                return switch (st) {
                    case "responded" -> responded;
                    case "pending" -> !responded;
                    case "archived" -> archived;
                    default -> true;
                };
            }).toList();
        }
        if (q != null && !q.isBlank()) {
            String term = q.toLowerCase();
            all = all.stream().filter(r ->
                    (r.getDriverName() != null && r.getDriverName().toLowerCase().contains(term)) ||
                            (r.getParkingName() != null && r.getParkingName().toLowerCase().contains(term)) ||
                            (r.getComment() != null && r.getComment().toLowerCase().contains(term))
            ).toList();
        }

        java.time.Instant gte = null;
        java.time.Instant lte = null;
        try { if (createdGte != null) gte = java.time.Instant.parse(createdGte); } catch (Exception ignored) {}
        try { if (createdLte != null) lte = java.time.Instant.parse(createdLte); } catch (Exception ignored) {}
        final java.time.Instant finalGte = gte;
        final java.time.Instant finalLte = lte;
        if (finalGte != null) {
            all = all.stream().filter(r -> r.getCreatedAt() != null && !r.getCreatedAt().toInstant().isBefore(finalGte)).toList();
        }
        if (finalLte != null) {
            all = all.stream().filter(r -> r.getCreatedAt() != null && !r.getCreatedAt().toInstant().isAfter(finalLte)).toList();
        }

        java.util.Comparator<Review> comparator = "rating".equalsIgnoreCase(sort)
                ? java.util.Comparator.comparing(Review::getRating, java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder()))
                : java.util.Comparator.comparing(r -> r.getCreatedAt(), java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder()));
        if ("desc".equalsIgnoreCase(order)) comparator = comparator.reversed();
        all = all.stream().sorted(comparator).toList();

        int total = all.size();
        int from = Math.max(0, (page - 1) * limit);
        int to = Math.min(total, from + limit);
        List<Review> slice = from >= total ? List.of() : all.subList(from, to);

        var parkingMap = parkingRepository.findAllById(slice.stream().map(Review::getParkingId).collect(Collectors.toSet()))
                .stream().collect(Collectors.toMap(Parking::getId, p -> p));

        var resources = slice.stream()
                .map(r -> ReviewResourceFromEntityAssembler.toResourceFromEntity(r, parkingMap.get(r.getParkingId())))
                .toList();

        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(total))
                .body(resources);
    }

    @GetMapping("/kpis")
    @Operation(summary = "Get reviews KPIs (owner view)")
    public ResponseEntity<java.util.Map<String, Object>> getKpis(@RequestParam(required = false) String currentUserId) {
        List<Review> all = reviewRepository.findAll();
        if (currentUserId != null && !currentUserId.isBlank()) {
            try {
                Long ownerId = Long.parseLong(currentUserId);
                Set<Long> parkingIds = parkingRepository.findParkingsByOwnerId(new OwnerId(ownerId))
                        .stream().map(Parking::getId).collect(Collectors.toSet());
                all = all.stream().filter(r -> parkingIds.contains(r.getParkingId())).toList();
            } catch (NumberFormatException ignored) {}
        }
        double avg = all.stream().filter(r -> r.getRating() != null).mapToDouble(Review::getRating).average().orElse(0);
        long total = all.size();
        long responded = all.stream().filter(r -> Boolean.TRUE.equals(r.getResponded())).count();
        java.util.Map<String, Object> body = new java.util.HashMap<>();
        body.put("averageRating", avg);
        body.put("averageRatingDelta", 0);
        body.put("totalReviews", total);
        body.put("totalReviewsDelta", 0);
        body.put("responseRate", total == 0 ? 0 : (responded * 100.0) / total);
        body.put("responseRateDelta", 0);
        body.put("avgResponseTimeHours", 0);
        body.put("avgResponseTimeDelta", 0);
        return ResponseEntity.ok(body);
    }

    @PatchMapping("/{id}/respond")
    @Operation(summary = "Respond to a review")
    public ResponseEntity<ReviewResource> respondReview(@PathVariable Long id, @RequestBody java.util.Map<String, Object> body) {
        var reviewOpt = reviewRepository.findById(id);
        if (reviewOpt.isEmpty()) return ResponseEntity.notFound().build();
        var review = reviewOpt.get();
        String text = String.valueOf(body.getOrDefault("responseText", ""));
        review.respondWithText(text);
        var saved = reviewRepository.save(review);
        var parking = parkingRepository.findById(saved.getParkingId()).orElse(null);
        return ResponseEntity.ok(ReviewResourceFromEntityAssembler.toResourceFromEntity(saved, parking));
    }

    @PatchMapping("/{id}/read")
    @Operation(summary = "Mark a review as read")
    public ResponseEntity<ReviewResource> markRead(@PathVariable Long id) {
        var reviewOpt = reviewRepository.findById(id);
        if (reviewOpt.isEmpty()) return ResponseEntity.notFound().build();
        var review = reviewOpt.get();
        review.markRead();
        var saved = reviewRepository.save(review);
        var parking = parkingRepository.findById(saved.getParkingId()).orElse(null);
        return ResponseEntity.ok(ReviewResourceFromEntityAssembler.toResourceFromEntity(saved, parking));
    }

    @PatchMapping("/{id}/archive")
    @Operation(summary = "Archive a review")
    public ResponseEntity<ReviewResource> archive(@PathVariable Long id) {
        var reviewOpt = reviewRepository.findById(id);
        if (reviewOpt.isEmpty()) return ResponseEntity.notFound().build();
        var review = reviewOpt.get();
        review.archiveNow();
        var saved = reviewRepository.save(review);
        var parking = parkingRepository.findById(saved.getParkingId()).orElse(null);
        return ResponseEntity.ok(ReviewResourceFromEntityAssembler.toResourceFromEntity(saved, parking));
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
                        new ResponseEntity<>(ReviewResourceFromEntityAssembler.toResourceFromEntity(source,
                                parkingRepository.findById(source.getParkingId()).orElse(null)), CREATED))
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

        var parking = parkingRepository.findById(parkingId).orElse(null);
        List<ReviewResource> resources = reviews.stream()
                .map(r -> ReviewResourceFromEntityAssembler.toResourceFromEntity(r, parking))
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
                .map(r -> ReviewResourceFromEntityAssembler.toResourceFromEntity(r,
                        parkingRepository.findById(r.getParkingId()).orElse(null)))
                .toList();
        return ResponseEntity.ok(resources);
    }
}
