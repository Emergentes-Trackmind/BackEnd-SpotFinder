package com.spotfinder.backend.v1.parkingManagement.interfaces.rest.transform;

import com.spotfinder.backend.v1.parkingManagement.domain.model.commands.AssignIotSensorCommand;
import com.spotfinder.backend.v1.parkingManagement.interfaces.rest.resources.AssignIotSensorResource;

import java.util.UUID;

public class AssignIotSensorCommandFromResourceAssembler {
    public static AssignIotSensorCommand toCommandFromResource(AssignIotSensorResource resource, UUID spotId) {
        return new AssignIotSensorCommand(spotId, resource.sensorSerialNumber());
    }
}

