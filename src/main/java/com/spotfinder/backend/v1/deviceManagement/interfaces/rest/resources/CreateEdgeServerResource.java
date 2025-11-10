package com.spotfinder.backend.v1.deviceManagement.interfaces.rest.resources;

public record CreateEdgeServerResource(
        String serverId,
        String apiKey,
        String name,
        String macAddress,
        String status,
        Long parkingId
        ) {

}
