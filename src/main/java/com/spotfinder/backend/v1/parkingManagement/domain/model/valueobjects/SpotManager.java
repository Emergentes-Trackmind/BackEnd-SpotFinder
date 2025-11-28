package com.spotfinder.backend.v1.parkingManagement.domain.model.valueobjects;

import com.spotfinder.backend.v1.parkingManagement.domain.model.aggregates.Parking;
import com.spotfinder.backend.v1.parkingManagement.domain.model.entities.ParkingSpot;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Embeddable;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Embeddable
@Getter
public class SpotManager {

    @OneToMany(mappedBy = "parking", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<ParkingSpot> parkingSpots;

    public SpotManager() {
        this.parkingSpots = new ArrayList<>();
    }

    // Explicit getter method to ensure it's available
    public List<ParkingSpot> getParkingSpots() {
        return parkingSpots;
    }

    public ParkingSpot addParkingSpot(Parking parking, Integer row, Integer column, String label) {
        ParkingSpot parkingSpot = new ParkingSpot(parking, row, column, label);
        this.parkingSpots.add(parkingSpot);
        return parkingSpot;
    }

    public void removeParkingSpot(UUID parkingSpotId) {
        this.parkingSpots.removeIf(parkingSpot -> parkingSpot.getId().equals(parkingSpotId));
    }

    public ParkingSpot getParkingSpotById(UUID parkingSpotId) {
        return this.parkingSpots.stream()
                .filter(parkingSpot -> parkingSpot.getId().equals(parkingSpotId))
                .findFirst()
                .orElse(null);
    }
}
