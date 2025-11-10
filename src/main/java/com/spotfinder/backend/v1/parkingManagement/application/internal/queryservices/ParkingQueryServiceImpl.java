package com.spotfinder.backend.v1.parkingManagement.application.internal.queryservices;

import com.spotfinder.backend.v1.parkingManagement.domain.model.aggregates.Parking;
import com.spotfinder.backend.v1.parkingManagement.domain.model.entities.ParkingSpot;
import com.spotfinder.backend.v1.parkingManagement.domain.model.queries.*;
import com.spotfinder.backend.v1.parkingManagement.domain.model.valueobjects.OwnerId;
import com.spotfinder.backend.v1.parkingManagement.domain.services.ParkingQueryService;
import com.spotfinder.backend.v1.parkingManagement.infrastructure.persistence.jpa.repositories.ParkingRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ParkingQueryServiceImpl implements ParkingQueryService {

    private final ParkingRepository parkingRepository;

    public ParkingQueryServiceImpl(ParkingRepository parkingRepository) {
        this.parkingRepository = parkingRepository;
    }


    @Override
    public Optional<Parking> handle(GetParkingByIdQuery query) {
        return this.parkingRepository.findById(query.parkingId());
    }

    @Override
    public List<Parking> handle(GetParkingsByOwnerIdQuery query) {
        OwnerId ownerId = new OwnerId(query.ownerId());
        return this.parkingRepository.findParkingsByOwnerId(ownerId);
    }

    @Override
    public List<Parking> handle(GetAllParkingQuery query) {
        return this.parkingRepository.findAll();
    }

    @Override
    public List<ParkingSpot> handle(GetParkingSpotsByParkingIdQuery query) {
        var parking = this.parkingRepository.findById(query.parkingId());
        if (parking.isPresent()) {
            return parking.get().getParkingSpots();
        }
        return List.of();
    }

    @Override
    public Optional<ParkingSpot> handle(GetParkingSpotByIdQuery query) {
        var parking = this.parkingRepository.findById(query.parkingId());
        return parking.flatMap(value -> value.getParkingSpots().stream()
                .filter(parkingSpot -> parkingSpot.getId().equals(query.parkingSpotId()))
                .findFirst());
    }
}
