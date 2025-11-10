package com.spotfinder.backend.v1.parkingManagement.domain.services;

import com.spotfinder.backend.v1.parkingManagement.domain.model.aggregates.Parking;
import com.spotfinder.backend.v1.parkingManagement.domain.model.entities.ParkingSpot;
import com.spotfinder.backend.v1.parkingManagement.domain.model.queries.*;

import java.util.List;
import java.util.Optional;

public interface ParkingQueryService {

    Optional<Parking> handle(GetParkingByIdQuery query);
    List<Parking> handle(GetParkingsByOwnerIdQuery query);
    List<Parking> handle(GetAllParkingQuery query);

    List<ParkingSpot> handle(GetParkingSpotsByParkingIdQuery query);
    Optional<ParkingSpot> handle(GetParkingSpotByIdQuery query);
}