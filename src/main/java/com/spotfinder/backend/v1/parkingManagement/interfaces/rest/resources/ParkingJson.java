package com.spotfinder.backend.v1.parkingManagement.interfaces.rest.resources;

public record ParkingJson(
        String id,
        String ownerId,
        String name,
        String type,
        String description,
        Integer totalSpaces,
        Integer accessibleSpaces,
        String phone,
        String email,
        String website,
        String status
) {}
