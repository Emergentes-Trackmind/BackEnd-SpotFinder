package com.spotfinder.backend.v1.reservations.interfaces.rest;

import com.spotfinder.backend.v1.parkingManagement.domain.model.aggregates.Parking;
import com.spotfinder.backend.v1.parkingManagement.domain.model.valueobjects.OwnerId;
import com.spotfinder.backend.v1.parkingManagement.infrastructure.persistence.jpa.repositories.ParkingRepository;
import com.spotfinder.backend.v1.reservations.domain.model.aggregates.Reservation;
import com.spotfinder.backend.v1.reservations.domain.model.queries.GetAllReservationsByDriverIdAndStatusQuery;
import com.spotfinder.backend.v1.reservations.domain.model.queries.GetAllReservationsByParkingIdQuery;
import com.spotfinder.backend.v1.reservations.domain.model.queries.GetAllReservationsQuery;
import com.spotfinder.backend.v1.reservations.domain.services.ReservationCommandService;
import com.spotfinder.backend.v1.reservations.domain.services.ReservationQueryService;
import com.spotfinder.backend.v1.reservations.infrastructure.persistence.jpa.repositories.ReservationRepository;
import com.spotfinder.backend.v1.reservations.interfaces.rest.resources.CreateReservationResource;
import com.spotfinder.backend.v1.reservations.interfaces.rest.resources.ReservationResource;
import com.spotfinder.backend.v1.reservations.interfaces.rest.transform.CreateReservationCommandFromResourceAssembler;
import com.spotfinder.backend.v1.reservations.interfaces.rest.transform.ReservationResourceFromEntityAssembler;
import com.spotfinder.backend.v1.reservations.interfaces.rest.transform.UpdateReservationCommandFromResourceAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value = {"/api/v1/reservations", "/api/reservations"}, produces = APPLICATION_JSON_VALUE)
@Tag(name = "Reservation", description = "Reservation management")
public class ReservationsController {
    private final ReservationCommandService reservationCommandService;
    private final ReservationQueryService reservationQueryService;
    private final ParkingRepository parkingRepository;
    private final ReservationRepository reservationRepository;

    public ReservationsController(ReservationCommandService reservationCommandService,
                                  ReservationQueryService reservationQueryService,
                                  ParkingRepository parkingRepository,
                                  ReservationRepository reservationRepository) {
        this.reservationCommandService = reservationCommandService;
        this.reservationQueryService = reservationQueryService;
        this.parkingRepository = parkingRepository;
        this.reservationRepository = reservationRepository;
    }

    @GetMapping
    @Operation(summary = "List reservations (owner view compatible with web frontend)")
    public ResponseEntity<List<ReservationResource>> listReservations(
            @RequestParam(required = false) String currentUserId,
            @RequestParam(required = false) Long parkingOwnerId,
            @RequestParam(required = false) List<String> status,
            @RequestParam(required = false) Long parkingId,
            @RequestParam(required = false) String q,
            @RequestParam(name = "startTime_gte", required = false) String startGte,
            @RequestParam(name = "startTime_lte", required = false) String startLte,
            @RequestParam(name = "_page", defaultValue = "1") int page,
            @RequestParam(name = "_limit", defaultValue = "10") int limit,
            @RequestParam(name = "_sort", defaultValue = "createdAt") String sort,
            @RequestParam(name = "_order", defaultValue = "desc") String order
    ) {
        List<Reservation> all = reservationQueryService.handle(new GetAllReservationsQuery());

        // Filter by owner (currentUserId)
        Long ownerFilter = null;
        if (currentUserId != null && !currentUserId.isBlank()) {
            try {
                ownerFilter = Long.parseLong(currentUserId);
            } catch (NumberFormatException ignored) {}
        }
        if (parkingOwnerId != null) {
            ownerFilter = parkingOwnerId;
        }
        if (ownerFilter != null) {
            List<Parking> ownerParkings = parkingRepository.findParkingsByOwnerId(new OwnerId(ownerFilter));
            Set<Long> ownerParkingIds = ownerParkings.stream().map(Parking::getId).collect(Collectors.toSet());
            all = all.stream().filter(r -> ownerParkingIds.contains(r.getParkingId())).toList();
        }

        if (parkingId != null) {
            all = all.stream().filter(r -> r.getParkingId().equals(parkingId)).toList();
        }

        if (status != null && !status.isEmpty()) {
            Set<String> normalized = status.stream().map(String::toUpperCase).collect(Collectors.toSet());
            all = all.stream().filter(r -> normalized.contains(r.getStatus().toUpperCase())).toList();
        }

        if (q != null && !q.isBlank()) {
            String term = q.toLowerCase();
            all = all.stream().filter(r ->
                    (r.getDriverName() != null && r.getDriverName().toLowerCase().contains(term)) ||
                            (r.getVehiclePlate() != null && r.getVehiclePlate().toLowerCase().contains(term)) ||
                            (r.getSpotLabel() != null && r.getSpotLabel().toLowerCase().contains(term))
            ).toList();
        }

        LocalDateTime gte = null;
        LocalDateTime lte = null;
        try { if (startGte != null) gte = LocalDateTime.parse(startGte); } catch (Exception ignored) {}
        try { if (startLte != null) lte = LocalDateTime.parse(startLte); } catch (Exception ignored) {}
        final LocalDateTime finalGte = gte;
        final LocalDateTime finalLte = lte;
        if (finalGte != null) {
            all = all.stream().filter(r -> !LocalDateTime.of(r.getDate(), r.getStartTime()).isBefore(finalGte)).toList();
        }
        if (finalLte != null) {
            all = all.stream().filter(r -> !LocalDateTime.of(r.getDate(), r.getStartTime()).isAfter(finalLte)).toList();
        }

        Comparator<Reservation> comparator;
        if ("startTime".equalsIgnoreCase(sort)) {
            comparator = Comparator.comparing(r -> LocalDateTime.of(r.getDate(), r.getStartTime()));
        } else {
            comparator = Comparator.comparing(Reservation::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder()));
        }
        if ("desc".equalsIgnoreCase(order)) comparator = comparator.reversed();
        all = all.stream().sorted(comparator).toList();

        int total = all.size();
        int from = Math.max(0, (page - 1) * limit);
        int to = Math.min(total, from + limit);
        List<Reservation> slice = from >= total ? List.of() : all.subList(from, to);

        var parkingMap = parkingRepository.findAllById(slice.stream().map(Reservation::getParkingId).collect(Collectors.toSet()))
                .stream().collect(Collectors.toMap(Parking::getId, p -> p));

        List<ReservationResource> resources = slice.stream()
                .map(r -> ReservationResourceFromEntityAssembler.toResourceFromEntity(r, parkingMap.get(r.getParkingId())))
                .toList();

        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(total))
                .body(resources);
    }

    @Operation(summary = "Create a new reservation")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Reservation created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    @PostMapping
    public ResponseEntity<ReservationResource> createReservation(@RequestBody CreateReservationResource resource) throws IOException {
        Optional<Reservation> reservation = this.reservationCommandService
                .handle(CreateReservationCommandFromResourceAssembler.toCommandFromResource(resource));

        return reservation.map(source ->
                        new ResponseEntity<>(ReservationResourceFromEntityAssembler.toResourceFromEntity(source,
                                parkingRepository.findById(source.getParkingId()).orElse(null)), CREATED))
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    @Operation(summary = "Get reservation by id (compatible with web frontend)")
    @GetMapping("/{reservationId}")
    public ResponseEntity<ReservationResource> getReservationById(@PathVariable Long reservationId) {
        return reservationRepository.findById(reservationId)
                .map(reservation -> ReservationResourceFromEntityAssembler.toResourceFromEntity(reservation,
                        parkingRepository.findById(reservation.getParkingId()).orElse(null)))
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Get all reservations by parking id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reservations retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "No reservations found")
    })
    @GetMapping("/parking/{parkingId}")
    public ResponseEntity<List<ReservationResource>> getReservationsByParkingId(@PathVariable Long parkingId) {
        var query = new GetAllReservationsByParkingIdQuery(parkingId);
        List<Reservation> reservations = this.reservationQueryService.handle(query);

        if (reservations.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<ReservationResource> resources = reservations.stream()
                .map(r -> ReservationResourceFromEntityAssembler.toResourceFromEntity(r,
                        parkingRepository.findById(r.getParkingId()).orElse(null)))
                .toList();
        return ResponseEntity.ok(resources);
    }

    @Operation(summary = "Get all reservations by driver id and status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reservations retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "No reservations found")
    })
    @GetMapping("/driver/{driverId}/status/{status}")
    public ResponseEntity<List<ReservationResource>> getReservationsByDriverIdAndStatus(
            @PathVariable Long driverId, @PathVariable String status) {
        var query = new GetAllReservationsByDriverIdAndStatusQuery(driverId, status);
        List<Reservation> reservations = this.reservationQueryService.handle(query);

        if (reservations.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<ReservationResource> resources = reservations.stream()
                .map(r -> ReservationResourceFromEntityAssembler.toResourceFromEntity(r,
                        parkingRepository.findById(r.getParkingId()).orElse(null)))
                .toList();
        return ResponseEntity.ok(resources);
    }

    @Operation(summary = "Update a reservation status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reservation status updated successfully"),
            @ApiResponse(responseCode = "404", description = "Reservation not found")
    })
    @PatchMapping("/{reservationId}")
    public ResponseEntity<ReservationResource> updateReservationStatus(
            @PathVariable Long reservationId,
            @RequestBody(required = false) Map<String, Object> body,
            @RequestParam(value = "status", required = false) String statusParam) throws IOException {
        String status = statusParam;
        if ((status == null || status.isBlank()) && body != null) {
            status = String.valueOf(body.getOrDefault("status", ""));
        }

        if (status == null || status.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        Optional<Reservation> updatedReservation;
        try {
            updatedReservation = this.reservationCommandService
                    .handle(UpdateReservationCommandFromResourceAssembler.toCommandFromResource(reservationId, status));
        } catch (IllegalArgumentException ex) {
            if ("Reservation not found".equalsIgnoreCase(ex.getMessage())) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().build();
        }

        return updatedReservation.map(reservation ->
                        ResponseEntity.ok(ReservationResourceFromEntityAssembler.toResourceFromEntity(reservation,
                                parkingRepository.findById(reservation.getParkingId()).orElse(null))))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
