package com.spotfinder.backend.v1.iam.interfaces.rest.transform;

import com.spotfinder.backend.v1.iam.domain.model.aggregates.User;
import com.spotfinder.backend.v1.iam.domain.model.entities.Role;
import com.spotfinder.backend.v1.iam.interfaces.rest.resources.UserResource;

public class UserResourceFromEntityAssembler {
    public static UserResource toResourceFromEntity(User user) {
        var roles = user.getRoles().stream().map(Role::getStringName).toList();
        return new UserResource(user.getId(), user.getEmail(), roles);
    }
}