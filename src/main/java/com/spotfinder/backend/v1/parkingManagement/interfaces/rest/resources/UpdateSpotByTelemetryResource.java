package com.spotfinder.backend.v1.parkingManagement.interfaces.rest.resources;

public record UpdateSpotByTelemetryResource(String sensorSerialNumber, boolean occupied) {
}

