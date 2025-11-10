package com.spotfinder.backend.v1.parkingManagement.infrastructure.persistence.jpa.repositories;

import com.spotfinder.backend.v1.parkingManagement.domain.model.aggregates.Parking;
import com.spotfinder.backend.v1.parkingManagement.domain.model.valueobjects.OwnerId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ParkingRepository extends JpaRepository<Parking, Long> {
    List<Parking> findParkingsByOwnerId(OwnerId ownerId);
    
    @org.springframework.data.jpa.repository.Query("SELECT p FROM Parking p WHERE p.name = :name AND p.ownerId.value = :ownerIdValue")
    java.util.Optional<Parking> findByNameAndOwnerIdValue(String name, Long ownerIdValue);
}
