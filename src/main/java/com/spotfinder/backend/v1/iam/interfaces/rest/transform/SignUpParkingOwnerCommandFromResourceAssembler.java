package com.spotfinder.backend.v1.iam.interfaces.rest.transform;

import com.spotfinder.backend.v1.iam.domain.model.commands.SignUpParkingOwnerCommand;
import com.spotfinder.backend.v1.iam.interfaces.rest.resources.SignUpParkingOwnerResource;

public class SignUpParkingOwnerCommandFromResourceAssembler {
    public static SignUpParkingOwnerCommand toCommandFromResource(SignUpParkingOwnerResource resource) {
        return new SignUpParkingOwnerCommand(resource.fullName(), resource.email() ,resource.password(),
                resource.city(), resource.country(), resource.phone(), resource.companyName(),resource.ruc());
    }
}
