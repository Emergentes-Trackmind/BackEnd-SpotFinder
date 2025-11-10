package com.spotfinder.backend.v1.reviews.application.internal.outboundservices.acl;

import com.spotfinder.backend.v1.parkingManagement.interfaces.acl.ParkingSpotContextFacade;
import org.springframework.stereotype.Service;

@Service("externalParkingServiceReview")
public class ExternalParkingService {
    private final ParkingSpotContextFacade parkingSpotContextFacade;

    public ExternalParkingService(ParkingSpotContextFacade parkingSpotContextFacade) {
        this.parkingSpotContextFacade = parkingSpotContextFacade;
    }

    public String getParkingName(Long parkingId) {
        return parkingSpotContextFacade.getParkingName(parkingId);
    }

    public void updateParkingRating(Long parkingId, Float rating) {
        parkingSpotContextFacade.updateParkingRating(parkingId, rating);
    }
}
