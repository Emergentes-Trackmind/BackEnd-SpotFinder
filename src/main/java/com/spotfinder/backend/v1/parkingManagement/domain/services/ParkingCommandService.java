package com.spotfinder.backend.v1.parkingManagement.domain.services;

import com.spotfinder.backend.v1.parkingManagement.domain.model.aggregates.Parking;
import com.spotfinder.backend.v1.parkingManagement.domain.model.commands.*;
import com.spotfinder.backend.v1.parkingManagement.domain.model.entities.ParkingSpot;

import java.util.Optional;

public interface ParkingCommandService {

    Optional<Parking> handle(CreateParkingCommand command);

    Optional<ParkingSpot> handle(AddParkingSpotCommand command);

    Optional<String> handle(UpdateParkingSpotAvailabilityCommand command);

    Optional<String> handle(UpdateAvailableParkingSpotCountCommand command);

    Optional<String> handle(UpdateParkingRatingCommand command);
}
