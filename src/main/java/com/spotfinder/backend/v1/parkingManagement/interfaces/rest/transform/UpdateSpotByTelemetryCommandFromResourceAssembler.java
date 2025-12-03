package com.spotfinder.backend.v1.parkingManagement.interfaces.rest.transform;

import com.spotfinder.backend.v1.parkingManagement.domain.model.commands.UpdateSpotByTelemetryCommand;
import com.spotfinder.backend.v1.parkingManagement.interfaces.rest.resources.UpdateSpotByTelemetryResource;

public class UpdateSpotByTelemetryCommandFromResourceAssembler {
    public static UpdateSpotByTelemetryCommand toCommandFromResource(UpdateSpotByTelemetryResource resource) {
        return new UpdateSpotByTelemetryCommand(resource.sensorSerialNumber(), resource.occupied());
    }
}

