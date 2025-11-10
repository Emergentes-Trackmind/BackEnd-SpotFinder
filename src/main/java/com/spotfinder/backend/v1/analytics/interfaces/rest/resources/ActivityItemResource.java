package com.spotfinder.backend.v1.analytics.interfaces.rest.resources;

public record ActivityItemResource(
        String id,
        String type,
        String title,
        String description,
        String userName,
        String userAvatar,
        String status,
        String createdAt,
        RelatedEntity relatedEntity
) {
    public record RelatedEntity(String id, String name, String type) {}
}

