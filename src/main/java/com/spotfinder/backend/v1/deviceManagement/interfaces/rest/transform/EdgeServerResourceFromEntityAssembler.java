package com.spotfinder.backend.v1.deviceManagement.interfaces.rest.transform;

import com.spotfinder.backend.v1.deviceManagement.domain.model.aggregates.EdgeServer;
import com.spotfinder.backend.v1.deviceManagement.interfaces.rest.resources.EdgeServerResource;

public class EdgeServerResourceFromEntityAssembler {

    public static EdgeServerResource toResourceFromEntity(EdgeServer entity) {
        return new EdgeServerResource(
                entity.getId(),
                entity.getServerId(),
                entity.getApiKey(),
                entity.getName(),
                entity.getMacAddress(),
                entity.getStatus().name(),
                entity.getLastHeartBeat(),
                entity.getConnectedDevicesCount(),
                entity.getParkingId()
        );
    }
}
