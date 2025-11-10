package com.spotfinder.backend.v1.profile.interfaces.rest.transform;


import com.spotfinder.backend.v1.profile.domain.model.aggregates.Driver;
import com.spotfinder.backend.v1.profile.interfaces.rest.resource.DriverResource;

public class DriverResourceFromEntityAssembler {
    public static DriverResource toResourceFromEntity(Driver driver) {
        return new DriverResource(
                driver.getUserId(), driver.getId(), driver.getFullName(),
                driver.getCity(), driver.getCountry(), driver.getPhone(),
                driver.getDni()
        );
    }
}
