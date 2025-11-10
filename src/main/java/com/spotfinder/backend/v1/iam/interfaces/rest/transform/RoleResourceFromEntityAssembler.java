package com.spotfinder.backend.v1.iam.interfaces.rest.transform;


import com.spotfinder.backend.v1.iam.domain.model.entities.Role;
import com.spotfinder.backend.v1.iam.interfaces.rest.resources.RoleResource;

public class RoleResourceFromEntityAssembler {
    public static RoleResource toResourceFromEntity(Role role) {
        return new RoleResource(role.getId(), role.getStringName());
    }
}