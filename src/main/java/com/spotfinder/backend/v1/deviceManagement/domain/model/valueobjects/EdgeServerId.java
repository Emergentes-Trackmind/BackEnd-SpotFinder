package com.spotfinder.backend.v1.deviceManagement.domain.model.valueobjects;

import jakarta.persistence.Embeddable;

@Embeddable
public record EdgeServerId(String edgeServerId) {
    public EdgeServerId() {
        this("unassigned");
    }
}
