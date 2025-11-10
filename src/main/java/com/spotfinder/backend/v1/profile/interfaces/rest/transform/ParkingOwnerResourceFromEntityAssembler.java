package com.spotfinder.backend.v1.profile.interfaces.rest.transform;


import com.spotfinder.backend.v1.profile.domain.model.aggregates.ParkingOwner;
import com.spotfinder.backend.v1.profile.interfaces.rest.resource.ParkingOwnerResource;

public class ParkingOwnerResourceFromEntityAssembler {
    public static ParkingOwnerResource toResourceFromEntity(ParkingOwner parkingOwner) {
        return new ParkingOwnerResource(
                parkingOwner.getUserId(), parkingOwner.getId(), parkingOwner.getFullName(), parkingOwner.getCity(),
                parkingOwner.getCountry(), parkingOwner.getPhone(), parkingOwner.getCompanyName(), parkingOwner.getRuc()
        );
    }
}
