package com.spotfinder.backend.v1.deviceManagement.domain.model.commands;

import java.util.UUID;

public record CreateDeviceCommand(
        Long parkingId, UUID parkingSpotId, String spotStatus, String spotLabel, String edgeServerId
) {
}
