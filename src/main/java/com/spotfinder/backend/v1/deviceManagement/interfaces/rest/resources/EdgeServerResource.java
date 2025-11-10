package com.spotfinder.backend.v1.deviceManagement.interfaces.rest.resources;

public record EdgeServerResource(
        Long id,
        String serverId,
        String apiKey,
        String name,
        String ipAddress,
        String status,
        String lastHeartbeat,
        Integer connectedDevicesCount,
        Long parkingId
) {
}
