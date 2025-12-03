package com.spotfinder.backend.v1.parkingManagement.domain.model.commands;

public record UpdateSpotByTelemetryCommand(String sensorSerialNumber, boolean occupied) {
}

