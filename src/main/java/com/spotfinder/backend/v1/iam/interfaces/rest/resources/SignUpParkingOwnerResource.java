package com.spotfinder.backend.v1.iam.interfaces.rest.resources;

public record SignUpParkingOwnerResource(
        String email,
        String password,
        String fullName,
        String city,
        String country,
        String phone,
        String companyName,
        String ruc
) {
}
