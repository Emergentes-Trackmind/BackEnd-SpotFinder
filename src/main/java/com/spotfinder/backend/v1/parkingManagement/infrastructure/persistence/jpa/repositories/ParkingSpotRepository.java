package com.spotfinder.backend.v1.parkingManagement.infrastructure.persistence.jpa.repositories;

import com.spotfinder.backend.v1.parkingManagement.domain.model.entities.ParkingSpot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ParkingSpotRepository extends JpaRepository<ParkingSpot, UUID> {
    Optional<ParkingSpot> findBySensorSerialNumber(String sensorSerialNumber);
    boolean existsBySensorSerialNumber(String sensorSerialNumber);
}

