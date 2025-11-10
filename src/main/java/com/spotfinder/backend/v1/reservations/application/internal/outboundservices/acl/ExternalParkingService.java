package com.spotfinder.backend.v1.reservations.application.internal.outboundservices.acl;

import com.spotfinder.backend.v1.parkingManagement.interfaces.acl.ParkingSpotContextFacade;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ExternalParkingService {
    private final ParkingSpotContextFacade parkingSpotContextFacade;

    public ExternalParkingService(ParkingSpotContextFacade parkingSpotContextFacade) {
        this.parkingSpotContextFacade = parkingSpotContextFacade;
    }

    public Float getParkingRatePerHour(Long parkingId) {
        return this.parkingSpotContextFacade.getParkingRatePerHour(parkingId);
    }

    public String getSpotLabel(UUID parkingSpotId, Long parkingId) {
        return this.parkingSpotContextFacade.getSpotLabel(parkingSpotId, parkingId);
    }

    public void updateParkingSpotAvailability(Long parkingId, UUID parkingSpotId, String availability) {
        this.parkingSpotContextFacade.updateParkingSpotAvailability(parkingId, parkingSpotId, availability);
    }

    public void updateAvailableSpotsCount(Long parkingId, Integer availableSpots, String operation) {
        this.parkingSpotContextFacade.updateAvailableParkingSpotCount(parkingId, availableSpots, operation);
    }

    public Long getOwnerUserIdByParkingId(Long parkingId) {
        return this.parkingSpotContextFacade.getOwnerUserIdByParkingId(parkingId);
    }


}
