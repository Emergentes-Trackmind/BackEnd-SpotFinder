package com.spotfinder.backend.v1.reservations.interfaces.rest;

import com.spotfinder.backend.v1.reservations.domain.model.aggregates.Reservation;
import com.spotfinder.backend.v1.reservations.domain.model.queries.GetAllReservationsByDriverIdAndStatusQuery;
import com.spotfinder.backend.v1.reservations.domain.model.queries.GetAllReservationsByParkingIdQuery;
import com.spotfinder.backend.v1.reservations.domain.services.ReservationCommandService;
import com.spotfinder.backend.v1.reservations.domain.services.ReservationQueryService;
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
import java.util.List;
import java.util.Optional;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value = "/api/v1/reservations", produces = APPLICATION_JSON_VALUE)
@Tag(name = "Reservation", description = "Reservation management")
public class ReservationsController {
    private final ReservationCommandService reservationCommandService;
    private final ReservationQueryService reservationQueryService;

    public ReservationsController(ReservationCommandService reservationCommandService, ReservationQueryService reservationQueryService) {
        this.reservationCommandService = reservationCommandService;
        this.reservationQueryService = reservationQueryService;
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
                        new ResponseEntity<>(ReservationResourceFromEntityAssembler.toResourceFromEntity(source), CREATED))
                .orElseGet(() -> ResponseEntity.badRequest().build());
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
                .map(ReservationResourceFromEntityAssembler::toResourceFromEntity)
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
                .map(ReservationResourceFromEntityAssembler::toResourceFromEntity)
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
            @PathVariable Long reservationId, @RequestParam String status) throws IOException {
        Optional<Reservation> updatedReservation = this.reservationCommandService
                .handle(UpdateReservationCommandFromResourceAssembler.toCommandFromResource(reservationId, status));

        return updatedReservation.map(reservation ->
                        ResponseEntity.ok(ReservationResourceFromEntityAssembler.toResourceFromEntity(reservation)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
