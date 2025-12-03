package com.spotfinder.backend.v1.parkingManagement.domain.model.commands;

import java.util.UUID;

public record AssignIotSensorCommand(UUID spotId, String sensorSerialNumber) {
}

