package com.spotfinder.backend.v1.deviceManagement.domain.model.queries;

import java.util.UUID;

public record GetDeviceByParkingSpotIdQuery(UUID parkingSpotId) {
}
