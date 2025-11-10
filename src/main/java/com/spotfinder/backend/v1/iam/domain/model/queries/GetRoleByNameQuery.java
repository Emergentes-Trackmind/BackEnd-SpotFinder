package com.spotfinder.backend.v1.iam.domain.model.queries;

import com.spotfinder.backend.v1.iam.domain.model.valueobjects.Roles;

public record GetRoleByNameQuery(Roles name) {
}
