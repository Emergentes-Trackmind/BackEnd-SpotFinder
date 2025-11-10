package com.spotfinder.backend.v1.deviceManagement.interfaces.rest.transform;

import com.spotfinder.backend.v1.deviceManagement.domain.model.commands.CreateEdgeServerCommand;
import com.spotfinder.backend.v1.deviceManagement.interfaces.rest.resources.CreateEdgeServerResource;

public class CreateEdgeServerCommandFromResourceAssembler {

    public static CreateEdgeServerCommand toCommandFromResource(CreateEdgeServerResource resource) {
        return new CreateEdgeServerCommand(
                resource.serverId(),
                resource.apiKey(),
                resource.name(),
                resource.macAddress(),
                resource.status(),
                resource.parkingId()
        );
    }
}
