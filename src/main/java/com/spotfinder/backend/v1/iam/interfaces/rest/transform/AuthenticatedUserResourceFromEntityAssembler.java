package com.spotfinder.backend.v1.iam.interfaces.rest.transform;


import com.spotfinder.backend.v1.iam.domain.model.aggregates.User;
import com.spotfinder.backend.v1.iam.interfaces.rest.resources.AuthenticatedUserResource;

public class AuthenticatedUserResourceFromEntityAssembler {
    public static AuthenticatedUserResource toResourceFromEntity(User user, String token) {
        return new AuthenticatedUserResource(user.getId(), user.getEmail(), token, user.getSerializedRoles());
    }
}
