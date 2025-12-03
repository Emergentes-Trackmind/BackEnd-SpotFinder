package com.spotfinder.backend.v1.parkingManagement.interfaces.rest.resources;

public record UpdateSpotByTelemetryResource(String serialNumber, boolean occupied) {
}
