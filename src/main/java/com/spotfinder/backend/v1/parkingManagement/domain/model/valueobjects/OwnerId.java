package com.spotfinder.backend.v1.parkingManagement.domain.model.valueobjects;

import jakarta.persistence.Embeddable;

@Embeddable
public class OwnerId {
    private Long value;

    public OwnerId() {}

    public OwnerId(Long value) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("Owner ID must be a positive number");
        }
        this.value = value;
    }

    public Long getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OwnerId ownerId = (OwnerId) o;
        return value.equals(ownerId.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}