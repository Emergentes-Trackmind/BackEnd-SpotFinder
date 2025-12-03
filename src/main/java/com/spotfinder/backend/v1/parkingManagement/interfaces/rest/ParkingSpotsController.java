package com.spotfinder.backend.v1.parkingManagement.interfaces.rest;

import com.spotfinder.backend.v1.parkingManagement.domain.services.ParkingCommandService;
import com.spotfinder.backend.v1.parkingManagement.interfaces.rest.resources.AssignIotSensorResource;
import com.spotfinder.backend.v1.parkingManagement.interfaces.rest.resources.ParkingSpotResource;
import com.spotfinder.backend.v1.parkingManagement.interfaces.rest.resources.UpdateSpotByTelemetryResource;
import com.spotfinder.backend.v1.parkingManagement.interfaces.rest.transform.AssignIotSensorCommandFromResourceAssembler;
import com.spotfinder.backend.v1.parkingManagement.interfaces.rest.transform.ParkingSpotResourceFromEntityAssembler;
import com.spotfinder.backend.v1.parkingManagement.interfaces.rest.transform.UpdateSpotByTelemetryCommandFromResourceAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value = {"/api/spots", "/api/v1/spots"}, produces = APPLICATION_JSON_VALUE)
@Tag(name = "Parking Spots", description = "Parking Spot IoT management")
public class ParkingSpotsController {

    private static final Logger logger = LoggerFactory.getLogger(ParkingSpotsController.class);

    private final ParkingCommandService parkingCommandService;

    public ParkingSpotsController(ParkingCommandService parkingCommandService) {
        this.parkingCommandService = parkingCommandService;
    }

    @Operation(summary = "Assign IoT sensor to a parking spot")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "IoT sensor assigned successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request or sensor already assigned"),
            @ApiResponse(responseCode = "404", description = "Parking spot not found")
    })
    @PutMapping("/{spotId}/assign-iot")
    public ResponseEntity<ParkingSpotResource> assignIotSensor(
            @PathVariable UUID spotId,
            @RequestBody AssignIotSensorResource resource) {

        var command = AssignIotSensorCommandFromResourceAssembler.toCommandFromResource(resource, spotId);
        var parkingSpot = parkingCommandService.handle(command);

        if (parkingSpot.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        var spotResource = ParkingSpotResourceFromEntityAssembler.toResourceFromEntity(parkingSpot.get());
        return new ResponseEntity<>(spotResource, OK);
    }

    @Operation(summary = "Sync parking spot status by telemetry data from Edge Server")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Telemetry data received and processed")
    })
    @PostMapping("/sync-telemetry")
    public ResponseEntity<Void> syncTelemetry(
            @RequestBody UpdateSpotByTelemetryResource resource) {

        try {
            var command = UpdateSpotByTelemetryCommandFromResourceAssembler.toCommandFromResource(resource);
            var parkingSpot = parkingCommandService.handle(command);

            if (parkingSpot.isEmpty()) {
                logger.warn("Telemetry sync: Parking spot not found for sensor serial number: {}",
                           resource.serialNumber());
            } else {
                logger.info("Telemetry sync: Successfully updated spot {} with sensor {} to occupied: {}",
                           parkingSpot.get().getId(),
                           resource.serialNumber(),
                           resource.occupied());
            }
        } catch (IllegalArgumentException e) {
            logger.warn("Telemetry sync: Error processing telemetry data - {}", e.getMessage());
        } catch (Exception e) {
            logger.error("Telemetry sync: Unexpected error processing telemetry data", e);
        }

        return ResponseEntity.ok().build();
    }
}
