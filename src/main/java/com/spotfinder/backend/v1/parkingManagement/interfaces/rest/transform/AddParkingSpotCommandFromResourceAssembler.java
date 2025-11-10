package com.spotfinder.backend.v1.parkingManagement.interfaces.rest.transform;

import com.spotfinder.backend.v1.parkingManagement.domain.model.commands.AddParkingSpotCommand;
import com.spotfinder.backend.v1.parkingManagement.interfaces.rest.resources.AddParkingSpotResource;

public class AddParkingSpotCommandFromResourceAssembler {
    public static AddParkingSpotCommand toCommandFromResource(AddParkingSpotResource resource, Long parkingId) {
        return new AddParkingSpotCommand(
                resource.row(),
                resource.column(),
                resource.label(),
                parkingId
        );
    }
}