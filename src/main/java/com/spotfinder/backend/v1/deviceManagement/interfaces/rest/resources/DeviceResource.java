package com.spotfinder.backend.v1.deviceManagement.interfaces.rest.resources;

public record DeviceResource(
        Long id,
        String macAddress,
        String type,
        String operationalStatus,
        String spotStatus,
        String spotLabel,
        String parkingSpotId,
        Long parkingId,
        String edgeServerId,
        String lastCommunication
) {
}
